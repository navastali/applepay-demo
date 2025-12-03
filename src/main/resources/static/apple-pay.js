(function () {
  const STATUS = document.getElementById('status');
  const BTN_CONTAINER = document.getElementById('apple-pay-button-container');
  const VALIDATE_ENDPOINT = '/validate-merchant';
  const PAY_ENDPOINT = '/pay';

  const AMOUNT = 1990; // cents
  const CURRENCY = 'USD';

  function setStatus(msg) { STATUS.textContent = msg; }

  function supportsApplePay() {
    return window.ApplePaySession && ApplePaySession.canMakePayments();
  }

  function renderButton() {
    const btn = document.createElement('apple-pay-button');
    btn.setAttribute('type', 'plain');
    btn.setAttribute('buttonstyle', 'black');
    btn.setAttribute('locale', 'en-US');
    btn.addEventListener('click', onClick);
    BTN_CONTAINER.appendChild(btn);
  }

  async function onClick() {
    try {
      setStatus('Creating Apple Pay session...');
      const paymentRequest = {
        countryCode: 'US',
        currencyCode: CURRENCY,
        total: { label: 'Demo Merchant', amount: (AMOUNT/100).toFixed(2) },
        supportedNetworks: ['visa', 'masterCard', 'amex', 'discover'],
        merchantCapabilities: ['supports3DS']
      };

      const session = new ApplePaySession(3, paymentRequest);

      session.onvalidatemerchant = async (event) => {
        setStatus('Validating merchant...');
        try {
          const resp = await fetch(VALIDATE_ENDPOINT, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ validationURL: event.validationURL })
          });
          if (!resp.ok) throw new Error('Merchant validation failed');
          const merchantSession = await resp.json();
          session.completeMerchantValidation(merchantSession);
          setStatus('Merchant validated');
        } catch (err) {
          console.error(err);
          setStatus('Merchant validation failed');
          session.abort();
        }
      };

      session.onpaymentauthorized = async (event) => {
        setStatus('Payment authorized. Processing...');
        try {
          const token = event.payment.token; // send this to server
          const resp = await fetch(PAY_ENDPOINT, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ token: token, amount: AMOUNT, currency: CURRENCY })
          });
          const result = await resp.json();
          if (result.success) {
            session.completePayment(ApplePaySession.STATUS_SUCCESS);
            setStatus('Payment successful. PaymentIntent: ' + result.payment_intent_id);
          } else {
            session.completePayment(ApplePaySession.STATUS_FAILURE);
            setStatus('Payment failed: ' + (result.message || JSON.stringify(result)));
          }
        } catch (err) {
          console.error(err);
          session.completePayment(ApplePaySession.STATUS_FAILURE);
          setStatus('Payment processing error');
        }
      };

      session.begin();
    } catch (err) {
      console.error(err);
      setStatus('Failed to start Apple Pay session');
    }
  }

  // init
  (function init() {
    if (supportsApplePay()) {
      setStatus('Apple Pay is available on this device.');
      renderButton();
    } else {
      setStatus('Apple Pay is not available in this browser/device. Test in Safari on macOS/iOS with a supported card.');
    }
  })();
})();