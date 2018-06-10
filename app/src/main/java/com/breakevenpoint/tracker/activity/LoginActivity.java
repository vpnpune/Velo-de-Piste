package com.breakevenpoint.tracker.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.breakevenpoint.tracker.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    boolean BETA_ENVIORNMENT = true;


    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "dc_admin", "dc_raghu", "dc_divya", "dc_dantus"
    };
    List<String> AUTH_USERS = new ArrayList<>(Arrays.asList(DUMMY_CREDENTIALS));


    // Keep track of the login task to ensure we can cancel it if requested.
    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView mUserNameView,mRiderNameView,mRiderNumberView;
    private TextInputLayout input_user_name, input_password, input_rider_name,input_rider_number;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button login_button;
    //private EditText mRiderNameView;
    //private EditText mRiderNumberView;

    static String riderName;
    static String riderNummber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    public void initView() {
        mLoginFormView = findViewById(R.id.form_login);
        mProgressView = findViewById(R.id.progress_login);
        mUserNameView = findViewById(R.id.tv_user_name);
        mPasswordView = findViewById(R.id.tv_password);

        mRiderNumberView = findViewById(R.id.tv_rider_no);
        mRiderNameView = findViewById(R.id.tv_rider_name);

        input_user_name = findViewById(R.id.input_user_name);
        input_password = findViewById(R.id.input_password);
        input_rider_name = findViewById(R.id.input_rider_name);
        input_rider_number = findViewById(R.id.input_rider_no);

        if(TextUtils.isEmpty(MapHomeActivity.RIDER_NAME)){
            mRiderNumberView.setText(MapHomeActivity.RIDER_NUMBER);
            mRiderNameView.setText(MapHomeActivity.RIDER_NAME);
        }


        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mRiderNameView = findViewById(R.id.tv_rider_name);
        mRiderNumberView = findViewById(R.id.tv_rider_no);



        login_button = findViewById(R.id.btn_login);
        login_button.setOnClickListener(this);
        Button forgot_password = findViewById(R.id.btn_forgot_password);
        forgot_password.setOnClickListener(this);
        Button register = findViewById(R.id.btn_forgot_register);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                attemptLogin();
                //Toast.makeText(this, "Maps", Toast.LENGTH_SHORT).show();

                break;

            case R.id.btn_forgot_password:
                Snackbar.make(v, getString(R.string.snackbar_forgot_password), Snackbar.LENGTH_LONG)
                        .setAction("^_^", null).show();
                break;

            case R.id.btn_forgot_register:
                Snackbar.make(v, getString(R.string.snackbar_register), Snackbar.LENGTH_LONG)
                        .setAction("^_^", null).show();
                break;
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form. If there are form errors
     * (invalid email, missing fields, etc.), the errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        input_user_name.setError(null);
        input_password.setError(null);

        String userName = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();

         riderName = mRiderNameView.getText().toString();
         riderNummber = mRiderNumberView.getText().toString();
        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(riderName)) {
            input_rider_name.setError("Enter Rider Name");
            focusView = mRiderNameView;
            cancel = true;
        } else if (TextUtils.isEmpty(riderNummber)) {
            input_rider_number.setError("Enter Rider Number");
            focusView = mRiderNumberView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
            return;
        }

         /*boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(userName)) {
            input_user_name.setError(getString(R.string.error_no_name));
            focusView = mUserNameView;
            cancel = true;
        } else if (!isPhoneValid(userName) && !isEmailValid(userName)) {
            input_user_name.setError(getString(R.string.error_invalid_name));
            focusView = mUserNameView;
            cancel = true;
        } else if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            input_password.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        } else if ((isPhoneValid(userName) || isEmailValid(userName)) && TextUtils.isEmpty(password)) {
            input_password.setError(getString(R.string.error_no_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            hideInput(login_button);
            showProgress(true);
            mAuthTask = new UserLoginTask(userName, password);
            mAuthTask.execute((Void) null);
        }*/

        if (!BETA_ENVIORNMENT) {
            if (userName != null && !AUTH_USERS.contains(userName)) {
                Toast.makeText(this.getApplicationContext(), "Login Incorrect..! ", Toast.LENGTH_LONG).show();
                input_user_name.setError(getString(R.string.error_invalid_name));

                return;
            }
            if (password != null && !password.equalsIgnoreCase("dcadmin")) {
                Toast.makeText(this.getApplicationContext(), "Sorry..! Password Incorrect", Toast.LENGTH_LONG).show();
                input_password.setError(getString(R.string.error_no_password));

                return;
            }
        }
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);



    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                Intent i = new Intent(LoginActivity.this, MapHomeActivity.class);
                //i.putExtra("id", mEmailView.getText().toString());
                //i.putExtra("pass", mPasswordView.getText().toString());
                i.putExtra("riderName", riderName);
                i.putExtra("riderNumber", riderNummber);
                startActivity(i);
            } else {
                // User refused to grant permission. You can add AlertDialog here
                Toast.makeText(this, "You didn't give permission to access device location", Toast.LENGTH_LONG).show();

            }
        }
    }

    private boolean isPhoneValid(String userName) {
        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(userName);
        return m.matches() && userName.length() >= 7 && userName.length() <= 12;
    }

    private boolean isEmailValid(String userName) {
        return userName.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 4 && password.length() <= 20;
    }

    public void hideInput(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }


    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mPhone;
        private final String mPassword;

        UserLoginTask(String userName, String password) {
            mPhone = userName;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mPhone)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                input_password.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
