package com.ld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.amazonaws.logging.LogFactory;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.query.Where;
import com.amplifyframework.datastore.generated.model.Priority;
import com.amplifyframework.datastore.generated.model.aws;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "LDAWS";

    private String username = "zwj";
    private String pwd = "1234567z";
    private String email = "919353751@qq.com";
    private EditText mCodeEdit;
    private EditText accountEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogFactory.setLevel(LogFactory.Level.ALL);

        findViewById(R.id.register_code_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Amplify.Auth.signUp(username,
                        pwd,
                        AuthSignUpOptions.builder().userAttribute(AuthUserAttributeKey.email(), email).build(),
                        result -> Log.i("AuthQuickStart", "Result: " + result.toString()),
                        error -> Log.e("AuthQuickStart", "Sign up failed", error));
            }
        });

        mCodeEdit = (EditText) findViewById(R.id.code_et);
        accountEt = (EditText) findViewById(R.id.account_et);


        findViewById(R.id.register_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = accountEt.getText().toString().trim();
                Amplify.Auth.confirmSignUp(
                        username,
                        mCodeEdit.getText().toString(),
                        result -> {
                            Log.i("AuthQuickstart", result.isSignUpComplete() ? "Confirm signUp succeeded" : "Confirm sign up not complete");
                            startActivity(new Intent(MainActivity.this, IotTestActivity.class));
                        },
                        error -> Log.e("AuthQuickstart", error.toString())
                );
            }
        });

        findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Amplify.Auth.signIn(
                        username,
                        pwd,
                        result ->
                        {
                            Log.i("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete");
                            startActivity(new Intent(MainActivity.this, IotTestActivity.class));
                        },
                        error -> Log.e("AuthQuickstart", error.toString())
                );
            }
        });

        Amplify.Auth.fetchAuthSession(
                result -> Log.i("AmplifyQuickstart", result.toString()),
                error -> Log.e("AmplifyQuickstart", error.toString())
        );

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void test03() {
        Amplify.DataStore.observe(aws.class,
                started -> Log.i("Tutorial", "Observation began."),
                change -> Log.i("Tutorial", change.item().toString()),
                failure -> Log.e("Tutorial", "Observation failed.", failure),
                () -> Log.i("Tutorial", "Observation complete.")
        );
    }

    private void test02() {
        Amplify.DataStore.query(
                aws.class,
                Where.matches(
                        aws.PRIORITY.eq(Priority.HIGH)
                ),
                items -> {
                    while (items.hasNext()) {
                        aws next = items.next();
                        Log.i("Tutorial", "==== Todo ====");
                        Log.i("Tutorial", "Name: " + next.getName());

                        if (next.getPriority() != null) {
                            Log.i("Tutorial", "Priority: " + next.getPriority().toString());
                        }

                        if (next.getDescription() != null) {
                            Log.i("Tutorial", "Description: " + next.getDescription());
                        }
                    }
                },
                failure -> Log.e("Tutorial", "Could not query DataStore", failure)
        );
    }

    private void test01() {
        aws item = aws.builder()
                .name("Build Android application")
                .description("Build an Android application using Amplify,lalala")
                .build();
        aws item2 = aws.builder()
                .name("Finish quarterly taxes")
                .priority(Priority.HIGH)
                .description("Taxes are due for the quarter next week")
                .build();
        //保存到DataStore
        Amplify.DataStore.save(
                item,
                success -> Log.e("Tutorial", "Saved item: " + success.item().getName()),
                error -> Log.e("Tutorial", "Could not save item to DataStore", error)
        );
        Amplify.DataStore.save(
                item2,
                success -> Log.e("Tutorial", "Saved item: " + success.item().getName()),
                error -> Log.e("Tutorial", "Could not save item to DataStore", error)
        );
    }
}