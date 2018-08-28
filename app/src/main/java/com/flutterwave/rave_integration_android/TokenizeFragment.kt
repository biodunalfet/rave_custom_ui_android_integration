package com.flutterwave.rave_integration_android


import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import com.flutterwave.raveandroid.Payload
import com.flutterwave.raveandroid.PayloadBuilder
import com.flutterwave.raveandroid.RaveConstants
import com.flutterwave.raveandroid.Utils
import com.flutterwave.raveandroid.card.CardContract
import com.flutterwave.raveandroid.card.CardPresenter
import com.flutterwave.raveandroid.data.SavedCard
import com.flutterwave.raveandroid.responses.ChargeResponse
import com.flutterwave.raveandroid.responses.RequeryResponse


/**
 * A simple [Fragment] subclass.
 */
class TokenizeFragment : Fragment(), CardContract.View {

    //step 1
    val presenter by lazy {
        CardPresenter(activity, this)
    }

    val progressIndicator by lazy {
         ProgressDialog(activity)
    }

    lateinit var bottomSheetBehaviorVBV : BottomSheetBehavior<FrameLayout>

    var flwRef = ""
    var initialUrl : String = ""

    lateinit var v : View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_tokenize, container, false)

         bottomSheetBehaviorVBV = BottomSheetBehavior.from<FrameLayout>(v.findViewById(R.id.vbvBottomSheet))


        v.findViewById<Button>(R.id.payBtn).setOnClickListener {

        val cardNo = v.findViewById<EditText>(R.id.cardNoEt).text.toString()
        val cvv = v.findViewById<EditText>(R.id.cvvEt).text.toString()
        val expiry = v.findViewById<EditText>(R.id.cardExpEt).text.toString()
            val expiryYear = expiry.substring(3..4)
            val expiryMonth = expiry.substring(0..1)

            val builder = PayloadBuilder()
            builder.setAmount((activity as MainActivity).ravePayInitializer.amount.toString())
                    .setCardno(cardNo)
                    .setCountry((activity as MainActivity).ravePayInitializer.country.toString())
                    .setCurrency((activity as MainActivity).ravePayInitializer.currency.toString())
                    .setCvv(cvv)
                    .setEmail((activity as MainActivity).ravePayInitializer.email.toString())
                    .setFirstname((activity as MainActivity).ravePayInitializer.getfName().toString())
                    .setLastname((activity as MainActivity).ravePayInitializer.getlName().toString())
                    .setIP(Utils.getDeviceImei(this.activity!!))
                    .setTxRef((activity as MainActivity).ravePayInitializer.txRef.toString())
                    .setExpiryyear(expiryYear)
                    .setExpirymonth(expiryMonth)
                    .setPBFPubKey((activity as MainActivity).ravePayInitializer.publicKey)
//                    .setMeta("[]")
                    .setDevice_fingerprint(Utils.getDeviceImei(this.activity!!))

            val body = builder.createPayload()
            presenter.chargeCard(body, (activity as MainActivity).ravePayInitializer.secretKey)
        }

        return v
    }

    override fun showToast(msg: String?) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onRequerySuccessful(response: RequeryResponse?, responseAsJSONString: String?, flwRef: String?) {
        presenter.verifyRequeryResponse(response, responseAsJSONString, (activity as MainActivity).ravePayInitializer, flwRef)
    }

    override fun onNoAuthInternationalSuggested(payload: Payload?) {
        payload?.let {
            activity?.let {
                val bottomSheetDialog = BottomSheetDialog(it)
                val inflater = LayoutInflater.from(activity)

                val v = inflater.inflate( R.layout.avsvbv_layout, null, false)

                val addressEt = v.findViewById<TextInputEditText>(R.id.rave_billAddressEt)
                val stateEt = v.findViewById<TextInputEditText>(R.id.rave_billStateEt)
                val cityEt = v.findViewById<TextInputEditText>(R.id.rave_billCityEt)
                val zipCodeEt = v.findViewById<TextInputEditText>(R.id.rave_zipEt)
                val countryEt = v.findViewById<TextInputEditText>(R.id.rave_countryEt)
                val addressTil = v.findViewById<TextInputLayout>(R.id.rave_billAddressTil)
                val stateTil = v.findViewById<TextInputLayout>(R.id.rave_billStateTil)
                val cityTil = v.findViewById<TextInputLayout>(R.id.rave_billCityTil)
                val zipCodeTil = v.findViewById<TextInputLayout>(R.id.rave_zipTil)
                val countryTil = v.findViewById<TextInputLayout>(R.id.rave_countryTil)

                val zipBtn = v.findViewById<TextInputLayout>(R.id.rave_zipButton)

                zipBtn.setOnClickListener {

                    var valid = true

                    val address = addressEt.text.toString()
                    val state = stateEt.text.toString()
                    val city = cityEt.text.toString()
                    val zipCode = zipCodeEt.text.toString()
                    val country = countryEt.text.toString()

                    addressTil.error = null
                    stateTil.error = null
                    cityTil.error = null
                    zipCodeTil.error = null
                    countryTil.error = null

                    if (address.isEmpty()) {
                        valid = false
                        addressTil.error = "Enter a valid address"
                    }

                    if (state.isEmpty()) {
                        valid = false
                        stateTil.error = "Enter a valid state"
                    }

                    if (city.isEmpty()) {
                        valid = false
                        cityTil.error = "Enter a valid city"
                    }

                    if (zipCode.isEmpty()) {
                        valid = false
                        zipCodeTil.error = "Enter a valid zip code"
                    }

                    if (country.isEmpty()) {
                        valid = false
                        countryTil.error = "Enter a valid country"
                    }

                    if (valid) {
                        bottomSheetDialog.dismiss()
                        presenter.chargeCardWithAVSModel(payload, address, city, zipCode, country, state,
                                RaveConstants.NOAUTH_INTERNATIONAL, (activity as MainActivity).ravePayInitializer.secretKey)
                    }

                }

                bottomSheetDialog.setContentView(v)
                bottomSheetDialog.show()
            }

        }
    }

    //this method is called when the call to fetch the payment fee fails. You can decide to ignore or show a message
    override fun showFetchFeeFailed(message: String?) {
        showToast(message)
    }

    override fun onVBVAuthModelUsed(authUrlCrude: String?, flwRef: String?) {


        activity?.let {
            flwRef?.let {
                this.flwRef = it
            }

            v.findViewById<WebView>(R.id.webview).settings.loadsImagesAutomatically = true
            v.findViewById<WebView>(R.id.webview).settings.javaScriptEnabled = true
            v.findViewById<WebView>(R.id.webview).webViewClient = MyBrowser()
            v.findViewById<WebView>(R.id.webview).loadUrl(authUrlCrude)
            bottomSheetBehaviorVBV?.state = BottomSheetBehavior.STATE_EXPANDED
        }

    }

    inner class MyBrowser : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if (Build.VERSION.SDK_INT >= 21) {
                view.loadUrl(request.url.toString())
            }

            return true
        }

//        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
//            super.onPageStarted(view, url, favicon)
//            showFullProgressIndicator(true)
//        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            showFullProgressIndicator(true)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            showFullProgressIndicator(false)

            if (initialUrl.isEmpty()) {
                initialUrl = url
            } else if (url.contains("/complete") || url.contains("submitting_mock_form")) {
                bottomSheetBehaviorVBV.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBehaviorVBV.state = BottomSheetBehavior.STATE_HIDDEN

                presenter.requeryTx(flwRef, (activity as MainActivity).ravePayInitializer.secretKey, false)
            }

            Log.d("URLS", url)
        }
    }

    override fun onPaymentFailed(message: String?, responseAsJSONString: String?) {
        showToast(message)
    }

    override fun onTokenRetrievalError(p0: String?) {}

    override fun onValidateError(message: String?) {
        showToast(message)
    }

    override fun hideSavedCardsButton() {}

    override fun onNoAuthUsed(flwRef: String?, secretKey: String?) {
        presenter.requeryTx(flwRef, secretKey, false)
    }

    override fun showSavedCards(p0: MutableList<SavedCard>?) {}

    override fun onValidateCardChargeFailed(p0: String?, p1: String?) {}

    override fun onPaymentError(message: String?) {
        showToast(message)
    }

    override fun showOTPLayout(flwRef: String?, chargeResponseMessage : String) {

        //chargeResponseMessage = direction for the user
        v.findViewById<LinearLayout>(R.id.otpLayout).visibility = View.VISIBLE
        v.findViewById<Button>(R.id.otpBtn).setOnClickListener {
            val otp = v.findViewById<EditText>(R.id.otpEt).text.toString()
            presenter.validateCardCharge(flwRef, otp, (activity as MainActivity).ravePayInitializer.publicKey)
        }
    }

    override fun onPaymentSuccessful(status: String?, flwRef: String?, responseAsJSONString: String?) {
        // retrieve card details from `responseAsJSONString`
        Toast.makeText(activity, "payment successful", Toast.LENGTH_SHORT).show()
        Log.d("response_rave", responseAsJSONString.toString())
    }

    override fun onChargeTokenComplete(p0: ChargeResponse?) {}

    override fun showProgressIndicator(active: Boolean) {
        if (active) {
            progressIndicator.setMessage("please wait")
            progressIndicator.show()
        }
        else {
            progressIndicator.dismiss()
        }
    }

    /**
     * Called when the auth model suggested is AVS_VBVSecureCode. It opens a webview
     * that loads the authURL
     *
     * @param authurl = URL to display in webview
     * @param flwRef = reference of the payment transaction
     */
    override fun onAVSVBVSecureCodeModelUsed(authurl: String?, flwRef: String?) {
        activity?.let {
            flwRef?.let {
                this.flwRef = it
            }

            v.findViewById<WebView>(R.id.webview).settings.loadsImagesAutomatically = true
            v.findViewById<WebView>(R.id.webview).settings.javaScriptEnabled = true
            v.findViewById<WebView>(R.id.webview).webViewClient = MyBrowser()
            v.findViewById<WebView>(R.id.webview).loadUrl(authurl)
            bottomSheetBehaviorVBV?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun displayFee(p0: String?, p1: Payload?, p2: Int) {}

    override fun onChargeCardSuccessful(p0: ChargeResponse?) {
        presenter.requeryTx(flwRef, (activity as MainActivity).ravePayInitializer.secretKey, false)
    }

    override fun onAVS_VBVSECURECODEModelSuggested(payload: Payload?) {

        activity?.let {
            val bottomSheetDialog = BottomSheetDialog(it)
            val inflater = LayoutInflater.from(activity)

            val v = inflater.inflate( R.layout.avsvbv_layout, null, false)

            val addressEt = v.findViewById<TextInputEditText>(R.id.rave_billAddressEt)
            val stateEt = v.findViewById<TextInputEditText>(R.id.rave_billStateEt)
            val cityEt = v.findViewById<TextInputEditText>(R.id.rave_billCityEt)
            val zipCodeEt = v.findViewById<TextInputEditText>(R.id.rave_zipEt)
            val countryEt = v.findViewById<TextInputEditText>(R.id.rave_countryEt)
            val addressTil = v.findViewById<TextInputLayout>(R.id.rave_billAddressTil)
            val stateTil = v.findViewById<TextInputLayout>(R.id.rave_billStateTil)
            val cityTil = v.findViewById<TextInputLayout>(R.id.rave_billCityTil)
            val zipCodeTil = v.findViewById<TextInputLayout>(R.id.rave_zipTil)
            val countryTil = v.findViewById<TextInputLayout>(R.id.rave_countryTil)

            val zipBtn = v.findViewById<TextInputLayout>(R.id.rave_zipButton)

            zipBtn.setOnClickListener {

                var valid = true

                val address = addressEt.text.toString()
                val state = stateEt.text.toString()
                val city = cityEt.text.toString()
                val zipCode = zipCodeEt.text.toString()
                val country = countryEt.text.toString()

                addressTil.error = null
                stateTil.error = null
                cityTil.error = null
                zipCodeTil.error = null
                countryTil.error = null

                if (address.isEmpty()) {
                    valid = false
                    addressTil.error = "Enter a valid address"
                }

                if (state.isEmpty()) {
                    valid = false
                    stateTil.error = "Enter a valid state"
                }

                if (city.isEmpty()) {
                    valid = false
                    cityTil.error = "Enter a valid city"
                }

                if (zipCode.isEmpty()) {
                    valid = false
                    zipCodeTil.error = "Enter a valid zip code"
                }

                if (country.isEmpty()) {
                    valid = false
                    countryTil.error = "Enter a valid country"
                }

                if (valid) {
                    bottomSheetDialog.dismiss()
                    presenter.chargeCardWithAVSModel(payload, address, city, zipCode, country, state,
                            RaveConstants.NOAUTH_INTERNATIONAL, (activity as MainActivity).ravePayInitializer.secretKey)
                }

            }

            bottomSheetDialog.setContentView(v)
            bottomSheetDialog.show()
        }

    }

    override fun onTokenRetrieved(status: String?, p1: String?, p2: String?) {
    }

    override fun onValidateSuccessful(status: String?, p1: String?) {
        presenter.requeryTx(flwRef, (activity as MainActivity).ravePayInitializer.secretKey, false)
    }

    override fun showFullProgressIndicator(active: Boolean) {

        if (active) {
            v.findViewById<FrameLayout>(R.id.progressContainer).visibility = View.VISIBLE
        }
        else {
            v.findViewById<FrameLayout>(R.id.progressContainer).visibility = View.GONE
        }


    }

    override fun onPinAuthModelSuggested(payload: Payload?) {

        val builder = AlertDialog.Builder(activity!!)
        val inflater = LayoutInflater.from(activity)
        val v = inflater.inflate(com.flutterwave.raveandroid.R.layout.pin_layout, null, false)

        val pinBtn = v.findViewById<View>(com.flutterwave.raveandroid.R.id.rave_pinButton) as Button
        val pinEv = v.findViewById<View>(com.flutterwave.raveandroid.R.id.rave_pinEv) as TextInputEditText
        val pinTil = v.findViewById<View>(com.flutterwave.raveandroid.R.id.rave_pinTil) as TextInputLayout

        pinBtn.setOnClickListener {
            val pin = pinEv.text.toString()

            pinTil.error = null
            pinTil.isErrorEnabled = false

            if (pin.length != 4) {
                pinTil.error = "Enter a valid pin"
            } else {
                presenter.chargeCardWithSuggestedAuthModel(payload, pin, RaveConstants.PIN, (activity as MainActivity).ravePayInitializer.secretKey)
            }
        }

        builder.setView(v)
       builder.show()
    }


}// Required empty public constructor


