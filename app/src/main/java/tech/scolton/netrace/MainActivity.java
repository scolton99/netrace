package tech.scolton.netrace;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import tech.scolton.netrace.util.NDKTest;

public class MainActivity extends AppCompatActivity {
  private ConstraintLayout constraintLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    NavigationView navView = findViewById(R.id.nav_view);
    NavigationUI.setupWithNavController(navView, navController);

    final DrawerLayout drawer = findViewById(R.id.nav_drawer);

    Button openDrawer = findViewById(R.id.button);
    openDrawer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        drawer.openDrawer(GravityCompat.START);
      }
    });

    this.constraintLayout = findViewById(R.id.ConstraintLayout);

    Toast.makeText(this, NDKTest.test(), Toast.LENGTH_SHORT).show();
  }

  public void updateHolderConstraint(int bottom) {
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(this.constraintLayout);
    constraintSet.connect(R.id.fragmentHolder, ConstraintSet.BOTTOM, R.id.ConstraintLayout,
                          ConstraintSet.BOTTOM, bottom);
    constraintSet.applyTo(this.constraintLayout);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  static {
    System.loadLibrary("test-lib");
  }
}
