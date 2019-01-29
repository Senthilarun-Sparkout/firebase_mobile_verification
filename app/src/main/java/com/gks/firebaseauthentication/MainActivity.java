package com.gks.firebaseauthentication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.rilixtech.CountryCodePicker;

import java.util.concurrent.TimeUnit;

import static com.gks.firebaseauthentication.BaseClass.removeProgressDialog;
import static com.gks.firebaseauthentication.BaseClass.showSimpleProgressDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String mVerificationId, number = "";
    public PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;

    CountryCodePicker mCountryPicker;
    Button mBtnSendOtp, mBtnVerifyOtp, mBtnReSendOtp, mBtnTryAgain;
    EditText mVerifyOtp, mMobileNumber;
    LinearLayout mLlSendOtp, mLlVerifyOtp;
    TextView mTvMobileNumber, mTextVerified;
    RelativeLayout mRlVerified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        mLlSendOtp = findViewById(R.id.mLlSendOtp);
        mCountryPicker = findViewById(R.id.mCountryPicker);
        mMobileNumber = findViewById(R.id.mMobileNumber);
        mBtnSendOtp = findViewById(R.id.mBtnSendOtp);
        mLlVerifyOtp = findViewById(R.id.mLlVerifyOtp);
        mTvMobileNumber = findViewById(R.id.mTvMobileNumber);
        mBtnVerifyOtp = findViewById(R.id.mBtnVerifyOtp);
        mVerifyOtp = findViewById(R.id.mVerifyOtp);
        mBtnReSendOtp = findViewById(R.id.mBtnReSendOtp);
        mTextVerified = findViewById(R.id.mTextVerified);
        mRlVerified = findViewById(R.id.mRlVerified);
        mBtnTryAgain = findViewById(R.id.mBtnTryAgain);

        mBtnSendOtp.setOnClickListener(this);
        mBtnVerifyOtp.setOnClickListener(this);
        mBtnReSendOtp.setOnClickListener(this);
        mBtnTryAgain.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        /**
         * Callbacks used to know the status of request
         */
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.i("Arun", "onVerificationCompleted:" + credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("Arun", "onVerificationFailed", e);

                removeProgressDialog();

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                    Toast.makeText(MainActivity.this, "Invalid request", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                    Toast.makeText(MainActivity.this, "The SMS quota for the project has been exceeded", Toast.LENGTH_SHORT).show();
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.i("Arun", "onCodeSent:" + verificationId);

                removeProgressDialog();

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                mLlSendOtp.setVisibility(View.GONE);
                mLlVerifyOtp.setVisibility(View.VISIBLE);
                mTvMobileNumber.setText(mCountryPicker.getSelectedCountryCodeWithPlus() + " " + mMobileNumber.getText().toString());

                Toast.makeText(MainActivity.this, "Code Send Successfully", Toast.LENGTH_SHORT).show();

                // ...
            }
        };

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mBtnSendOtp:
                if (TextUtils.isEmpty(mMobileNumber.getText().toString())) {
                    mMobileNumber.setError("Enter Your Mobile Number");
                } else {
                    number = mCountryPicker.getSelectedCountryCodeWithPlus() + mMobileNumber.getText().toString();
                    SendOtp(number);
                }

                break;

            case R.id.mBtnReSendOtp:
                mVerifyOtp.setText("");
                resendVerificationCode(number, mResendToken);
                break;
            case R.id.mBtnVerifyOtp:
                String code = mVerifyOtp.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    mVerifyOtp.setError("Enter Otp you received");
                    return;
                }
                verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.mBtnTryAgain:
                clearAll();
                mRlVerified.setVisibility(View.GONE);
                mLlSendOtp.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Method to send OTP request to firebase
     * @param phoneNumber
     */
    private void SendOtp(String phoneNumber) {

        showSimpleProgressDialog(this);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    /**
     * Method to Resend OTP request to firebase
     * @param phoneNumber
     * @param token
     */
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {

        showSimpleProgressDialog(this);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    /**
     *
     * Send verificationId ( which you received in onCodeSent method in Callback
     * @param verificationId
     * @param code
     */
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        showSimpleProgressDialog(this);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    /**
     * Verify OTP
     * @param credential
     */
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        removeProgressDialog();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = task.getResult().getUser();

                            mLlVerifyOtp.setVisibility(View.GONE);
                            mRlVerified.setVisibility(View.VISIBLE);
                            mTextVerified.setText("OTP Verified Successfully \nfor " + number);
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("Arun", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(MainActivity.this, "Invalid Otp", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /**
     * Method for try Again
     */
    private void clearAll() {
        mMobileNumber.setText("");
        mTvMobileNumber.setText("");
        mVerifyOtp.setText("");
        mTextVerified.setText("");
        number = null;
        mVerificationId = null;
        mResendToken = null;
    }

}
