package com.example.dz.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dz.AppsBuffer;
import com.example.dz.MainActivity;
import com.example.dz.R;
import com.example.dz.model.App;
import com.example.dz.AppInformationBuffer;
import com.example.dz.AppPage;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    Context context;
    List<App> apps;

    public AppAdapter(Context context, List<App> apps) {
        this.context = context;
        this.apps = apps;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View applicationItems = LayoutInflater.from(context).inflate(R.layout.app_item, parent, false);
        return new AppViewHolder(applicationItems);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.applicationLogo.setImageDrawable(apps.get(position).getLogo());
        holder.applicationName.setText(apps.get(position).getName());
        if (apps.get(position).getIsEnable()) {
            holder.applicationEnableSwitch.setVisibility(View.VISIBLE);
            //}
            // Enable switch handler
            //{
            holder.applicationEnableSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                holder.applicationEnableSwitch.setVisibility(View.INVISIBLE);
                App currApp = AppsBuffer.INSTANCE.appsMap.get(apps.get(position).getPackageName());
                assert currApp != null;
                currApp.setIsEnable(false);
                AppsBuffer.INSTANCE.appsMap.replace(apps.get(position).getPackageName(), currApp);
            });
        }
        holder.itemView.setOnClickListener(view -> {
            Intent intent= new Intent(context, AppPage.class);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    (Activity) context,
                    new Pair<>(holder.itemView, "AppIconAndName")
            );

            AppInformationBuffer.INSTANCE.packageName = apps.get(position).getPackageName();
            AppInformationBuffer.INSTANCE.logoImage = apps.get(position).getLogo();
            AppInformationBuffer.INSTANCE.name = apps.get(position).getName();

            context.startActivity(intent, options.toBundle());
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public static final class AppViewHolder extends RecyclerView.ViewHolder {

        TextView    applicationName;
        ImageView   applicationLogo;
        Switch      applicationEnableSwitch;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);

            applicationName         = itemView.findViewById(R.id.applicationName);
            applicationLogo         = itemView.findViewById(R.id.applicationLogo);
            applicationEnableSwitch = itemView.findViewById(R.id.applicationEnableSwitch);
        }
    }

}
