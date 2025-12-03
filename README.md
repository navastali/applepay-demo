# Apple Pay + Stripe Starter (Spring Boot, Java 8) - Complete

This project is a runnable starter that demonstrates Apple Pay on the web with a Spring Boot backend and Stripe server-side processing.

## What this includes
- Spring Boot 2.7 (Java 8 compatible) project
- Endpoints:
  - `POST /validate-merchant` - performs Apple merchant validation using your `.p12` merchant identity certificate (mutual TLS)
  - `POST /pay` - converts Apple Pay token to Stripe Token and creates/confirms a PaymentIntent
- Static frontend (`/index.html`, `apple-pay.js`) served from `src/main/resources/static`

## Setup (you replace placeholders in application.properties)
1. Place your Apple merchant identity `.p12` at `src/main/resources/merchant_id.p12` and set `apple.merchant.p12.password` in `application.properties`.
2. Update `apple.merchant.id`, `apple.merchant.displayName`, `apple.merchant.domain` and `stripe.api.key` in `src/main/resources/application.properties`.
3. Register and verify your domain in Apple Developer portal and host `/.well-known/apple-developer-merchantid-domain-association` on your site.
4. Enable Apple Pay in Stripe Dashboard and add your domain there too.

## Running locally
- Build: `mvn clean package`
- Run: `java -jar target/applepay-stripe-0.0.1-SNAPSHOT.jar`
- Visit: `https://your-verified-domain/index.html` (Apple requires HTTPS and verified domain; localhost will not pass merchant validation)

## Security notes
- Do **not** commit real certificates or secret keys to source control.
- Use environment variables or a secure secrets store for production credentials.

## References
- Apple Pay JS: https://developer.apple.com/documentation/ApplePayontheWeb
- Stripe Apple Pay docs: https://stripe.com/docs/apple-pay
