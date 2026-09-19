package com.da_grupo9.ronda;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.da_grupo9.ronda.data.local.SessionManager;
import com.da_grupo9.ronda.data.local.SessionExpirationNotifier;
import com.da_grupo9.ronda.data.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject AuthRepository authRepository;
    @Inject SessionManager sessionManager;
    @Inject SessionExpirationNotifier sessionExpirationNotifier;

    private NavController navController;
    private final SessionExpirationNotifier.Listener sessionExpirationListener =
            this::redirectToLoginIfSessionExpired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View navHostFragmentView = findViewById(R.id.nav_host_fragment);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        ViewCompat.setOnApplyWindowInsetsListener(
                navHostFragmentView,
                (view, windowInsets) -> {
                    Insets systemBars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            0
                    );
                    return windowInsets;
                }
        );

        ViewCompat.setOnApplyWindowInsetsListener(
                bottomNavigationView,
                (view, windowInsets) -> {
                    Insets systemBars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    view.setPadding(
                            systemBars.left,
                            0,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return windowInsets;
                }
        );

        FragmentManager fragmentManager = getSupportFragmentManager();
        NavHostFragment navHostFragment =
                (NavHostFragment) fragmentManager.findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        if (savedInstanceState == null) {
            navHostFragmentView.setVisibility(View.INVISIBLE);
            if (sessionManager.isLoggedIn() && sessionManager.isBiometricEnabled()) {
                navHostFragmentView.setVisibility(View.VISIBLE);
            } else {
                authRepository.validarSesionGuardada(new AuthRepository.Resultado() {
                    @Override public void onSuccess() {
                        navController.navigate(R.id.action_loginFragment_to_homeFragment);
                        navHostFragmentView.setVisibility(View.VISIBLE);
                    }

                    @Override public void onError(String mensaje) {
                        navHostFragmentView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destinationId = destination.getId();

            boolean esDestinoConBottomNav =
                    destinationId == R.id.homeFragment
                            || destinationId == R.id.misPublicacionesFragment
                            || destinationId == R.id.publicarArticuloFragment
                            || destinationId == R.id.savedItemsFragment
                            || destinationId == R.id.profileFragment;

            bottomNavigationView.setVisibility(
                    esDestinoConBottomNav ? View.VISIBLE : View.GONE
            );
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        sessionExpirationNotifier.addListener(sessionExpirationListener);
        redirectToLoginIfSessionExpired();
    }

    @Override
    protected void onStop() {
        sessionExpirationNotifier.removeListener(sessionExpirationListener);
        super.onStop();
    }

    private void redirectToLoginIfSessionExpired() {
        if (!sessionExpirationNotifier.consumeExpiration()) return;
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == R.id.loginFragment) return;

        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(navController.getGraph().getId(), true)
                .build();
        navController.navigate(R.id.loginFragment, null, options);
        Toast.makeText(this, R.string.session_expired_message, Toast.LENGTH_LONG).show();
    }
}
