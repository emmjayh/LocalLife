package com.locallife.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.locallife.R;
import com.locallife.model.ShareableContent;
import com.locallife.model.ShareableContent.SharePlatform;
import com.locallife.service.SocialSharingService;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog fragment for selecting social media platforms to share content
 */
public class ShareDialogFragment extends DialogFragment {
    private static final String TAG = "ShareDialogFragment";
    private static final String ARG_CONTENT = "content";
    
    private ShareableContent content;
    private SocialSharingService socialSharingService;
    private SharePlatformAdapter adapter;
    private ShareResultListener listener;
    
    // UI Components
    private TextView titleText;
    private TextView previewText;
    private ImageView previewImage;
    private RecyclerView platformsRecyclerView;
    private Button shareButton;
    private Button cancelButton;
    private LinearLayout previewContainer;
    
    public interface ShareResultListener {
        void onShareSuccess(SharePlatform platform, String shareId);
        void onShareError(SharePlatform platform, Exception error);
        void onShareCancel();
    }
    
    public static ShareDialogFragment newInstance(ShareableContent content) {
        ShareDialogFragment fragment = new ShareDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            content = (ShareableContent) getArguments().getSerializable(ARG_CONTENT);
        }
        socialSharingService = new SocialSharingService(getContext());
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Share Your Achievement");
        return dialog;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_share_dialog, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupPreview();
        setupPlatforms();
        setupButtons();
    }
    
    private void initializeViews(View view) {
        titleText = view.findViewById(R.id.titleText);
        previewText = view.findViewById(R.id.previewText);
        previewImage = view.findViewById(R.id.previewImage);
        previewContainer = view.findViewById(R.id.previewContainer);
        platformsRecyclerView = view.findViewById(R.id.platformsRecyclerView);
        shareButton = view.findViewById(R.id.shareButton);
        cancelButton = view.findViewById(R.id.cancelButton);
    }
    
    private void setupPreview() {
        if (content != null) {
            titleText.setText(content.getTitle());
            previewText.setText(content.getFormattedShareText(SharePlatform.GENERIC));
            
            if (content.getImageBitmap() != null) {
                previewImage.setImageBitmap(content.getImageBitmap());
                previewImage.setVisibility(View.VISIBLE);
            } else {
                previewImage.setVisibility(View.GONE);
            }
        }
    }
    
    private void setupPlatforms() {
        List<PlatformInfo> platforms = getAvailablePlatforms();
        
        adapter = new SharePlatformAdapter(platforms, this::onPlatformSelected);
        platformsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        platformsRecyclerView.setAdapter(adapter);
    }
    
    private void setupButtons() {
        shareButton.setOnClickListener(v -> shareToSelectedPlatforms());
        cancelButton.setOnClickListener(v -> dismiss());
        
        // Initially disable share button
        shareButton.setEnabled(false);
        updateShareButtonState();
    }
    
    private List<PlatformInfo> getAvailablePlatforms() {
        List<PlatformInfo> platforms = new ArrayList<>();
        
        // Add platforms with their info
        platforms.add(new PlatformInfo(SharePlatform.TWITTER, "Twitter", 
            R.drawable.ic_twitter, "#1DA1F2"));
        platforms.add(new PlatformInfo(SharePlatform.FACEBOOK, "Facebook", 
            R.drawable.ic_facebook, "#1877F2"));
        platforms.add(new PlatformInfo(SharePlatform.INSTAGRAM, "Instagram", 
            R.drawable.ic_instagram, "#E4405F"));
        platforms.add(new PlatformInfo(SharePlatform.LINKEDIN, "LinkedIn", 
            R.drawable.ic_linkedin, "#0A66C2"));
        platforms.add(new PlatformInfo(SharePlatform.WHATSAPP, "WhatsApp", 
            R.drawable.ic_whatsapp, "#25D366"));
        platforms.add(new PlatformInfo(SharePlatform.TELEGRAM, "Telegram", 
            R.drawable.ic_telegram, "#0088CC"));
        
        // Filter out unavailable platforms
        List<PlatformInfo> availablePlatforms = new ArrayList<>();
        for (PlatformInfo info : platforms) {
            if (socialSharingService.isPlatformAvailable(info.platform)) {
                availablePlatforms.add(info);
            }
        }
        
        return availablePlatforms;
    }
    
    private void onPlatformSelected(PlatformInfo platformInfo, boolean isSelected) {
        platformInfo.isSelected = isSelected;
        updateShareButtonState();
    }
    
    private void updateShareButtonState() {
        boolean hasSelection = false;
        if (adapter != null) {
            for (PlatformInfo info : adapter.getPlatforms()) {
                if (info.isSelected) {
                    hasSelection = true;
                    break;
                }
            }
        }
        
        shareButton.setEnabled(hasSelection);
        shareButton.setText(hasSelection ? "Share" : "Select Platform");
    }
    
    private void shareToSelectedPlatforms() {
        if (adapter == null || content == null) return;
        
        List<PlatformInfo> selectedPlatforms = new ArrayList<>();
        for (PlatformInfo info : adapter.getPlatforms()) {
            if (info.isSelected) {
                selectedPlatforms.add(info);
            }
        }
        
        if (selectedPlatforms.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one platform", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Disable share button to prevent multiple clicks
        shareButton.setEnabled(false);
        shareButton.setText("Sharing...");
        
        // Share to each selected platform
        for (PlatformInfo platformInfo : selectedPlatforms) {
            socialSharingService.shareContent(content, platformInfo.platform, 
                new SocialSharingService.ShareResultCallback() {
                    @Override
                    public void onSuccess(SharePlatform platform, String shareId) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), 
                                    "Shared to " + platform.name(), 
                                    Toast.LENGTH_SHORT).show();
                                
                                if (listener != null) {
                                    listener.onShareSuccess(platform, shareId);
                                }
                            });
                        }
                    }
                    
                    @Override
                    public void onError(SharePlatform platform, Exception error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), 
                                    "Failed to share to " + platform.name() + ": " + error.getMessage(), 
                                    Toast.LENGTH_LONG).show();
                                
                                if (listener != null) {
                                    listener.onShareError(platform, error);
                                }
                            });
                        }
                    }
                    
                    @Override
                    public void onCancel(SharePlatform platform) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), 
                                    "Share to " + platform.name() + " cancelled", 
                                    Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
        }
        
        // Close dialog after a short delay
        if (getView() != null) {
            getView().postDelayed(() -> dismiss(), 1000);
        }
    }
    
    public void setShareResultListener(ShareResultListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if (listener != null) {
            listener.onShareCancel();
        }
    }
    
    // Platform info class
    public static class PlatformInfo {
        public final SharePlatform platform;
        public final String name;
        public final int iconResId;
        public final String color;
        public boolean isSelected = false;
        
        public PlatformInfo(SharePlatform platform, String name, int iconResId, String color) {
            this.platform = platform;
            this.name = name;
            this.iconResId = iconResId;
            this.color = color;
        }
    }
    
    // Platform adapter
    public static class SharePlatformAdapter extends RecyclerView.Adapter<SharePlatformAdapter.ViewHolder> {
        private List<PlatformInfo> platforms;
        private PlatformSelectionListener listener;
        
        public interface PlatformSelectionListener {
            void onPlatformSelected(PlatformInfo platform, boolean isSelected);
        }
        
        public SharePlatformAdapter(List<PlatformInfo> platforms, PlatformSelectionListener listener) {
            this.platforms = platforms;
            this.listener = listener;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_share_platform, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PlatformInfo platform = platforms.get(position);
            
            holder.iconView.setImageResource(platform.iconResId);
            holder.nameText.setText(platform.name);
            
            // Set selection state
            holder.containerView.setSelected(platform.isSelected);
            holder.containerView.setAlpha(platform.isSelected ? 1.0f : 0.7f);
            
            // Handle click
            holder.containerView.setOnClickListener(v -> {
                platform.isSelected = !platform.isSelected;
                notifyItemChanged(position);
                if (listener != null) {
                    listener.onPlatformSelected(platform, platform.isSelected);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return platforms.size();
        }
        
        public List<PlatformInfo> getPlatforms() {
            return platforms;
        }
        
        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View containerView;
            public final ImageView iconView;
            public final TextView nameText;
            
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                containerView = itemView.findViewById(R.id.containerView);
                iconView = itemView.findViewById(R.id.iconView);
                nameText = itemView.findViewById(R.id.nameText);
            }
        }
    }
}