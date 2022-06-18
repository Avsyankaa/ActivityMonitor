package com.example.dz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dz.model.App;
import com.google.android.material.slider.Slider;

public class AppPage extends AppCompatActivity {

    private App currApp;
    private boolean isUpdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Initialisation
        //{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_page);

        ImageView appImage              = findViewById(R.id.applicationLogo);
        TextView  appName               = findViewById(R.id.applicationName);
        TextView  appTimeLimitText      = findViewById(R.id.applicationTimeLimitText);
        Slider    appTimeLimitSlider    = findViewById(R.id.applicationTimeLimitSlider);
        Button    applySettingsButton   = findViewById(R.id.applySettingsButton);
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch    appEnableSwitch       = findViewById(R.id.applicationEnableSwitch);

        appImage.setImageDrawable(AppInformationBuffer.INSTANCE.logoImage);
        appName.setText(AppInformationBuffer.INSTANCE.name);
        //}
        // Get/Create app obj
        //{
        String pName = AppInformationBuffer.INSTANCE.packageName;
        if (AppsBuffer.INSTANCE.appsMap.containsKey(pName)) {
            currApp = AppsBuffer.INSTANCE.appsMap.get(pName);
            isUpdate = true;
        }
        else {
            currApp = new App(
                    pName,
                    AppInformationBuffer.INSTANCE.logoImage,
                    AppInformationBuffer.INSTANCE.name,
                    false, 0
            );
            isUpdate = false;
        }

        assert currApp != null;
        appEnableSwitch.setChecked(currApp.getIsEnable());
        if (appEnableSwitch.isChecked()) {
            appTimeLimitText.setVisibility(View.VISIBLE);
            appTimeLimitSlider.setVisibility(View.VISIBLE);
            appTimeLimitSlider.setValue(currApp.getTimeLimit());
        }

        //}
        // Enable switch handler
        //{
        appEnableSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                appTimeLimitText.setVisibility(View.VISIBLE);
                appTimeLimitSlider.setVisibility(View.VISIBLE);
            } else {
                currApp.setIsEnable(false);

                if (isUpdate)
                    AppsBuffer.INSTANCE.appsMap.replace(pName, currApp);
                else
                    AppsBuffer.INSTANCE.appsMap.put(pName, currApp);

                appTimeLimitText.setVisibility(View.INVISIBLE);
                appTimeLimitSlider.setVisibility(View.INVISIBLE);
                applySettingsButton.setVisibility(View.INVISIBLE);
            }
        });
        //}
        // Time limit slider handler
        //{
        appTimeLimitSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                if ((int) slider.getValue() != currApp.getTimeLimit())
                    applySettingsButton.setVisibility(View.VISIBLE);
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if ((int) slider.getValue() != currApp.getTimeLimit())
                    applySettingsButton.setVisibility(View.VISIBLE);
                else
                    applySettingsButton.setVisibility(View.INVISIBLE);
            }
        });
        //}
        // Apply button handler
        //{
        applySettingsButton.setOnClickListener(view -> {
            currApp.setIsEnable(true);
            currApp.setTimeLimit((int) appTimeLimitSlider.getValue());

            if (isUpdate)
                AppsBuffer.INSTANCE.appsMap.replace(pName, currApp);
            else
                AppsBuffer.INSTANCE.appsMap.put(pName, currApp);

            applySettingsButton.setVisibility(View.INVISIBLE);

            Toast.makeText(AppPage.this,
                    "Настройки успешно применены",
                    Toast.LENGTH_SHORT
            ).show();
        });
        //}
    }
}