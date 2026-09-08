package com.da_grupo9.ronda;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

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
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destinationId = destination.getId();

            boolean esDestinoConBottomNav =
                    destinationId == R.id.homeFragment
                            || destinationId == R.id.misPublicacionesFragment
                            || destinationId == R.id.publicarArticuloFragment
                            || destinationId == R.id.profileFragment;

            bottomNavigationView.setVisibility(
                    esDestinoConBottomNav ? View.VISIBLE : View.GONE
            );
        });
    }
}
