package com.toxic.search;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import java.util.LinkedHashMap;

/** Stable section containers avoid rebuilding unrelated cards and carousels. */
final class ProfileSectionsView {
    interface Builder { void build(LinearLayout target); }
    private final LinearLayout root;
    private final LinkedHashMap<String, Slot> slots = new LinkedHashMap<>();
    private static final class Slot {
        final LinearLayout view;
        String signature;
        Slot(LinearLayout view) { this.view = view; }
    }

    ProfileSectionsView(LinearLayout root) {
        this.root = root;
        root.removeAllViews();
        for (String key : new String[]{"progress", "header", "names", "mottos", "styles", "photos",
                "stats", "friends", "rooms", "groups", "badges"}) {
            LinearLayout view = new LinearLayout(root.getContext());
            view.setOrientation(LinearLayout.VERTICAL);
            view.setTag("profile_section_" + key);
            root.addView(view, new LinearLayout.LayoutParams(-1, -2));
            slots.put(key, new Slot(view));
        }
    }

    boolean owns(LinearLayout view) { return root == view; }

    void render(String key, String signature, Builder builder) {
        Slot slot = slots.get(key);
        if (slot == null || signature.equals(slot.signature)) return;
        slot.view.removeAllViews();
        builder.build(slot.view);
        slot.view.setVisibility(slot.view.getChildCount() == 0 ? View.GONE : View.VISIBLE);
        slot.signature = signature;
    }

    Anchor anchor(ScrollView scroll) {
        if (scroll == null || scroll.getScrollY() == 0) return null;
        int[] screen = new int[2], location = new int[2];
        scroll.getLocationOnScreen(screen);
        for (Slot slot : slots.values()) {
            if (slot.view.getVisibility() != View.VISIBLE) continue;
            slot.view.getLocationOnScreen(location);
            if (location[1] + slot.view.getHeight() > screen[1]) {
                return new Anchor(slot.view, location[1] - screen[1], scroll.getScrollY());
            }
        }
        return null;
    }

    static final class Anchor {
        final View view;
        final int offset, originalScroll;
        Anchor(View view, int offset, int originalScroll) {
            this.view = view; this.offset = offset; this.originalScroll = originalScroll;
        }
        void restore(ScrollView scroll) {
            scroll.post(() -> {
                // A touch/scroll after the update takes precedence over our anchor.
                if (!view.isAttachedToWindow() || scroll.getScrollY() != originalScroll) return;
                int[] parent = new int[2], current = new int[2];
                scroll.getLocationOnScreen(parent);
                view.getLocationOnScreen(current);
                int delta = current[1] - parent[1] - offset;
                if (delta != 0) scroll.scrollBy(0, delta);
            });
        }
    }
}
