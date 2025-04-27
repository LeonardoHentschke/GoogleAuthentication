package com.example.googleauthentication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    ShapeableImageView imageView;
    TextView name, mail, lastLoginTV, emailVerifiedTV;
    SignInButton signInButton;
    MaterialButton signOutButton;
    MaterialCardView userCard;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                        AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                        auth.signInWithCredential(authCredential).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                updateUI();
                                Toast.makeText(MainActivity.this, "Login feito com sucesso!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Falha ao fazer login: " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        userCard = findViewById(R.id.userCard);
        imageView = findViewById(R.id.profileImage);
        name = findViewById(R.id.nameTV);
        mail = findViewById(R.id.mailTV);
        lastLoginTV = findViewById(R.id.lastLoginTV);
        emailVerifiedTV = findViewById(R.id.emailVerifiedTV);
        signInButton = findViewById(R.id.signIn);
        signOutButton = findViewById(R.id.signout);

        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);

        signInButton.setOnClickListener(view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            activityResultLauncher.launch(intent);
        });

        signOutButton.setOnClickListener(view -> {
            auth.signOut();
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Toast.makeText(MainActivity.this, "Desconectado com sucesso!", Toast.LENGTH_SHORT).show();
                updateUI();
            });
        });

        updateUI(); // Atualiza UI baseado no estado atual (logado ou não)
    }

    private void animateView(View view, boolean show) {
        if (show) {
            view.setVisibility(View.VISIBLE);
            view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        } else {
            view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));
            view.setVisibility(View.GONE);
        }
    }

    private void updateUI() {
        if (auth.getCurrentUser() != null) {
            animateView(userCard, true);
            animateView(imageView, true);
            animateView(name, true);
            animateView(mail, true);
            animateView(lastLoginTV, true);
            animateView(emailVerifiedTV, true);
            animateView(signOutButton, true);
            animateView(signInButton, false);

            FirebaseUser user = auth.getCurrentUser();

            Glide.with(this)
                    .load(Objects.requireNonNull(user.getPhotoUrl()))
                    .into(imageView);

            name.setText(user.getDisplayName());
            mail.setText(user.getEmail());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            lastLoginTV.setText("Último login: " + sdf.format(new Date(user.getMetadata().getLastSignInTimestamp())));
            emailVerifiedTV.setText("Email verificado: " + (user.isEmailVerified() ? "✅ Sim" : "⚠️ Não"));
        } else {
            animateView(userCard, false);
            animateView(imageView, false);
            animateView(name, false);
            animateView(mail, false);
            animateView(lastLoginTV, false);
            animateView(emailVerifiedTV, false);
            animateView(signOutButton, false);
            animateView(signInButton, true);
        }
    }
}
