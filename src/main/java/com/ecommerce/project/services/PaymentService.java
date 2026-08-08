    package com.ecommerce.project.services;

    import com.ecommerce.project.enums.OrderStatus;
    import com.ecommerce.project.enums.PaymentStatus;
    import com.ecommerce.project.exceptions.APIException;
    import com.ecommerce.project.exceptions.ResourceNotFoundException;
    import com.ecommerce.project.model.*;
    import com.ecommerce.project.payload.PaymentIntentResponse;
    import com.ecommerce.project.repositories.*;
    import com.ecommerce.project.utils.AuthUtils;
    import com.stripe.Stripe;
    import com.stripe.StripeClient;
    import com.stripe.exception.SignatureVerificationException;
    import com.stripe.exception.StripeException;
    import com.stripe.model.Event;
    import com.stripe.model.EventDataObjectDeserializer;
    import com.stripe.model.PaymentIntent;
    import com.stripe.model.StripeObject;
    import com.stripe.net.Webhook;
    import com.stripe.param.PaymentIntentCreateParams;
    import jakarta.transaction.Transactional;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;

    @Service
    public class PaymentService {

        @Autowired
        private OrderRepository orderRepository;


        @Autowired
        private PaymentRepository paymentRepository ;

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private StripeClient stripeClient;

        @Autowired
        private CartRepository cartRepository;

        @Autowired
        private AuthUtils authUtils;

        @Autowired
        private CartItemRepository cartItemRepository;

        private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

        @Value("${spring.stripe.webhook.secret}")
        private String webHookSecret;



        @Transactional
        public PaymentIntentResponse createPaymentIntent(Long orderId) throws StripeException {

            Order order = orderRepository.findById(orderId).orElseThrow(()-> new ResourceNotFoundException("Order", "OrderId",orderId));

            String email = authUtils.loggedInEmail();

            if(!order.getEmail().equals(email)) throw new APIException("You are not allowed to pay this order");

            if(order.getOrderStatus() != OrderStatus.PENDING_PAYMENT){
                throw new APIException("Order is not waiting for payment");
            }

            Payment payment = order.getPayment();

            if(payment == null ){
                throw new APIException("No payment is associated with this order");
            }
            if(payment.getStatus() == PaymentStatus.SUCCEEDED) throw new APIException("Payment is already completed");


            if(payment.getPaymentIntentId() != null){
                PaymentIntent paymentIntent = stripeClient.v1().paymentIntents().retrieve(payment.getPaymentIntentId());
                return new PaymentIntentResponse(order.getId(),paymentIntent.getClientSecret(),payment.getPaymentIntentId());
            }
            long amount = Math.round(order.getTotalAmount() * 100);

            // build Stripe request
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount).setCurrency(payment.getCurrency()).putMetadata("orderId",order.getId().toString()).build();

            //create PaymentIntent
            PaymentIntent paymentIntent = stripeClient.v1().paymentIntents().create(params);

            payment.setPaymentIntentId(paymentIntent.getId());

            paymentRepository.save(payment);

            return new PaymentIntentResponse(order.getId(), paymentIntent.getClientSecret(), paymentIntent.getId());
        }


        @Transactional
        public void handleWebhook(String payload, String signature){
            Event event ;

            try {
                event = Webhook.constructEvent(
                        payload,
                        signature,
                        webHookSecret
                );
                System.out.println("Received event: " + event.getType());

            } catch (SignatureVerificationException e) {
                throw new APIException("Invalid Stripe signature");
            }catch(Exception e){
                e.printStackTrace();
                throw e;
            }
            switch (event.getType()){
                case "payment_intent.succeeded":
                    handleSuccessfulPayment(event);
                    break;
                case "payment_intent.payment_failed":
                    handleFailedPayment(event);
                    break;
            }
        }

        private void handleSuccessfulPayment(Event event){

            System.out.println(" ==== Handle Successfull Payment ====");


            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

            System.out.println("Stripe SDK API version: " + Stripe.API_VERSION);
            System.out.println("Event API version: " + event.getApiVersion());
            System.out.println("Event type: " + event.getType());

            StripeObject stripeObject = deserializer.getObject().orElse(null);

            if(stripeObject == null){
                log.error("Could not deserialize Stripe event. Event Type " + event.getType());
                log.error("Raw event data: {} ", deserializer.getRawJson());

                return;
            }

            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;

            System.out.println("Payment intent id  " + paymentIntent.getId());




            Payment payment = paymentRepository.findByPaymentIntentId(paymentIntent.getId());

            System.out.println("Payment found "  + payment);

            if(payment == null) throw new ResourceNotFoundException("Payment", "PaymentIntentId",paymentIntent.getId());

            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setPaidAt(LocalDateTime.now());

            Order order = payment.getOrder();

            order.setOrderStatus(OrderStatus.PAID);

            // reducing the stock
            for(OrderItem orderItem: order.getOrderItems()){
                Product product = orderItem.getProduct();

                product.setQuantity(product.getQuantity() - orderItem.getQuantity());

                productRepository.save(product);
            }

            //clearing the cart
            Cart cart = cartRepository.findCartByEmail(order.getEmail());
            if(cart != null){
                cartItemRepository.deleteByCartId(cart.getId());
                cart.getCartItems().clear();
                cart.setTotalPrice(0.0);
                cartRepository.save(cart);
            }

            log.info("Payment {} succeeded", paymentIntent.getId());

            log.info("Order {} marked as PAID", order.getId());

            paymentRepository.save(payment);
            orderRepository.save(order);

        }

        private void handleFailedPayment(Event event){
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = deserializer.getObject().orElseThrow();
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;

            Payment payment = paymentRepository.findByPaymentIntentId(paymentIntent.getId());

            if(payment == null) throw new ResourceNotFoundException("Payment", "PaymentIntentId",paymentIntent.getId());

            payment.setStatus(PaymentStatus.FAILED);
            payment.getOrder().setOrderStatus(OrderStatus.PAYMENT_FAILED);

            paymentRepository.save(payment);
            orderRepository.save(payment.getOrder());

        }

    }
