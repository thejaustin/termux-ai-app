package com.termux.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.termux.ai.R;
import com.termux.plus.api.TermuxPlugin;
import com.termux.plus.plugin.PluginManager;
import java.util.List;

public class PluginSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_settings);

        RecyclerView recyclerView = findViewById(R.id.plugin_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        PluginManager manager = PluginManager.getInstance(this);
        recyclerView.setAdapter(new PluginAdapter(manager.getPlugins(), manager));
    }

    private static class PluginAdapter extends RecyclerView.Adapter<PluginAdapter.ViewHolder> {
        private final List<TermuxPlugin> plugins;
        private final PluginManager manager;

        PluginAdapter(List<TermuxPlugin> plugins, PluginManager manager) {
            this.plugins = plugins;
            this.manager = manager;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_plugin, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TermuxPlugin plugin = plugins.get(position);
            holder.name.setText(plugin.getName());
            holder.description.setText(plugin.getDescription());
            holder.meta.setText("v" + plugin.getVersion() + " • " + plugin.getAuthor());
            holder.enabledSwitch.setChecked(plugin.isEnabled());

            holder.enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                manager.setPluginEnabled(plugin.getId(), isChecked);
            });
        }

        @Override
        public int getItemCount() {
            return plugins.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView description;
            TextView meta;
            SwitchMaterial enabledSwitch;

            ViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.plugin_name);
                description = itemView.findViewById(R.id.plugin_description);
                meta = itemView.findViewById(R.id.plugin_meta);
                enabledSwitch = itemView.findViewById(R.id.plugin_switch);
            }
        }
    }
}
