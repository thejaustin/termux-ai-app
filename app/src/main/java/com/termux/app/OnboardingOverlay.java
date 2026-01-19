package com.termux.app;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.termux.ai.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Onboarding overlay that shows gesture hints and app features on first launch.
 * Provides a slide-through tutorial to educate users about available gestures and features.
 */
public class OnboardingOverlay extends DialogFragment {

    private static final String PREFS_NAME = "onboarding_prefs";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";

    private ViewPager2 viewPager;
    private TabLayout pageIndicator;
    private Button btnSkip;
    private Button btnNext;

    private List<OnboardingPage> pages;

    public interface OnboardingCallback {
        void onOnboardingComplete();
    }

    private OnboardingCallback callback;

    public void setCallback(OnboardingCallback callback) {
        this.callback = callback;
    }

    /**
     * Check if onboarding has been completed before
     */
    public static boolean shouldShowOnboarding(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return !prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }

    /**
     * Mark onboarding as complete
     */
    public static void markOnboardingComplete(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply();
    }

    /**
     * Reset onboarding (for testing or settings)
     */
    public static void resetOnboarding(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, false).apply();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
        initializePages();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_onboarding, container, false);

        viewPager = view.findViewById(R.id.onboarding_viewpager);
        pageIndicator = view.findViewById(R.id.page_indicator);
        btnSkip = view.findViewById(R.id.btn_skip);
        btnNext = view.findViewById(R.id.btn_next);

        setupViewPager();
        setupButtons();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void initializePages() {
        pages = new ArrayList<>();

        // Page 1: Welcome
        pages.add(new OnboardingPage(
                R.drawable.ic_terminal,
                "Welcome to Termux AI",
                "An AI-enhanced terminal for Android.\nOptimized for Claude Code.",
                false
        ));

        // Page 2: Swipe Gestures
        pages.add(new OnboardingPage(
                R.drawable.ic_gesture_hint,
                "Swipe Navigation",
                "• Swipe LEFT/RIGHT to switch tabs\n" +
                "• Swipe DOWN to hide Claude overlay\n" +
                "• Long-press + FAB button to show tools",
                false
        ));

        // Page 3: Tab Management
        pages.add(new OnboardingPage(
                R.drawable.ic_add,
                "Tab Management",
                "• Tap + to create a new tab\n" +
                "• Long-press a tab to show close button\n" +
                "• Tap again to close it\n" +
                "• Each tab can have its own project",
                false
        ));

        // Page 4: AI Providers
        pages.add(new OnboardingPage(
                R.drawable.ic_claude,
                "Multi-AI Support",
                "Choose between Anthropic Claude and Google Gemini in Settings.\n\n" +
                "Get real-time suggestions and error analysis from either provider.",
                false
        ));

        // Page 5: Claude Code
        pages.add(new OnboardingPage(
                R.drawable.ic_code,
                "Claude Code CLI",
                "• Deep integration with Claude Code CLI\n" +
                "• Interactive coding & project analysis\n" +
                "• Visual progress indicators\n" +
                "• Shake to clear terminal",
                false
        ));

        // Page 6: Quick Actions
        pages.add(new OnboardingPage(
                R.drawable.ic_quick_commands,
                "Quick Tools",
                "• Long-press + button for tools panel\n" +
                "• 📁 File picker for context\n" +
                "• 🎤 Voice input\n" +
                "• ⚡ Quick commands\n" +
                "• ℹ️ Project information",
                false
        ));

        // Page 7: Get Started
        pages.add(new OnboardingPage(
                R.drawable.ic_check,
                "You're Ready!",
                "1. Go to Settings > AI Integration\n" +
                "2. Enter your API Key\n" +
                "3. Start coding with AI assistant!\n\n" +
                "Enjoy your enhanced terminal.",
                true
        ));
    }

    private void setupViewPager() {
        OnboardingPagerAdapter adapter = new OnboardingPagerAdapter();
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(pageIndicator, viewPager, (tab, position) -> {
            // Dots only, no text
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateButtonState(position);
            }
        });
    }

    private void setupButtons() {
        btnSkip.setOnClickListener(v -> completeOnboarding());

        btnNext.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < pages.size() - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                completeOnboarding();
            }
        });

        updateButtonState(0);
    }

    private void updateButtonState(int position) {
        if (position == pages.size() - 1) {
            btnNext.setText("Get Started");
            btnSkip.setVisibility(View.GONE);
        } else {
            btnNext.setText("Next");
            btnSkip.setVisibility(View.VISIBLE);
        }
    }

    private void completeOnboarding() {
        markOnboardingComplete(requireContext());
        if (callback != null) {
            callback.onOnboardingComplete();
        }
        dismiss();
    }

    /**
     * Data class for onboarding pages
     */
    private static class OnboardingPage {
        final int iconResId;
        final String title;
        final String description;
        final boolean isLastPage;

        OnboardingPage(int iconResId, String title, String description, boolean isLastPage) {
            this.iconResId = iconResId;
            this.title = title;
            this.description = description;
            this.isLastPage = isLastPage;
        }
    }

    /**
     * ViewPager adapter for onboarding pages
     */
    private class OnboardingPagerAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<OnboardingViewHolder> {

        @NonNull
        @Override
        public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding_page, parent, false);
            return new OnboardingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
            OnboardingPage page = pages.get(position);
            holder.bind(page);
        }

        @Override
        public int getItemCount() {
            return pages.size();
        }
    }

    /**
     * ViewHolder for onboarding pages
     */
    private static class OnboardingViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView title;
        private final TextView description;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.onboarding_icon);
            title = itemView.findViewById(R.id.onboarding_title);
            description = itemView.findViewById(R.id.onboarding_description);
        }

        void bind(OnboardingPage page) {
            icon.setImageResource(page.iconResId);
            title.setText(page.title);
            description.setText(page.description);

            // Add content descriptions for accessibility
            icon.setContentDescription(page.title + " icon");
            title.setContentDescription(page.title);
            description.setContentDescription(page.description);
        }
    }
}
