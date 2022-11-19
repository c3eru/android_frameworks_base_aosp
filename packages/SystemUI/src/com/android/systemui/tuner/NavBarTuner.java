/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.tuner;

import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.KEY;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.KEY_CODE_END;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.KEY_CODE_START;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.KEY_IMAGE_DELIM;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.MENU_IME;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.MENU_IME_ALWAYS_SHOW;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.NAVSPACE;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.NAV_BAR_LEFT;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.NAV_BAR_RIGHT;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.NAV_BAR_VIEWS;
<<<<<<< HEAD
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.RECENT;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.SEARCH;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.SIZE_MOD_END;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.SIZE_MOD_START;
=======
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.extractButton;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.extractImage;
import static com.android.systemui.statusbar.phone.NavigationBarInflaterView.extractKeycode;

import android.annotation.Nullable;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.EditText;

import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.NavigationBarInflaterView;
import com.android.systemui.tuner.TunerService.Tunable;

import java.util.ArrayList;

public class NavBarTuner extends TunerPreferenceFragment {

    private static final String LAYOUT = "layout";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";

    private static final String TYPE = "type";
    private static final String KEYCODE = "keycode";
    private static final String ICON = "icon";

    private static final int[][] ICONS = new int[][]{
            {R.drawable.ic_qs_circle, R.string.tuner_circle},
            {R.drawable.ic_add, R.string.tuner_plus},
            {R.drawable.ic_remove, R.string.tuner_minus},
            {R.drawable.ic_left, R.string.tuner_left},
            {R.drawable.ic_right, R.string.tuner_right},
            {R.drawable.ic_menu, R.string.tuner_menu},
    };

    private final ArrayList<Tunable> mTunables = new ArrayList<>();
    private Handler mHandler;

    @Override
<<<<<<< HEAD
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.nav_bar_tuner, container, false);
        inflatePreview((ViewGroup) view.findViewById(R.id.nav_preview_frame));
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        return view;
    }

    private void inflatePreview(ViewGroup view) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        boolean isRotated = display.getRotation() == Surface.ROTATION_90
                || display.getRotation() == Surface.ROTATION_270;

        Configuration config = new Configuration(getContext().getResources().getConfiguration());
        boolean isPhoneLandscape = isRotated && (config.smallestScreenWidthDp < 600);
        final float scale = isPhoneLandscape ? PREVIEW_SCALE_LANDSCAPE : PREVIEW_SCALE;
        config.densityDpi = (int) (config.densityDpi * scale);

        mPreview = (PreviewNavInflater) LayoutInflater.from(getContext().createConfigurationContext(
                config)).inflate(R.layout.nav_bar_tuner_inflater, view, false);
        final ViewGroup.LayoutParams layoutParams = mPreview.getLayoutParams();
        layoutParams.width = (int) ((isPhoneLandscape ? display.getHeight() : display.getWidth())
                * scale);
        // Not sure why, but the height dimen is not being scaled with the dp, set it manually
        // for now.
        layoutParams.height = (int) (layoutParams.height * scale);
        if (isPhoneLandscape) {
            int width = layoutParams.width;
            layoutParams.width = layoutParams.height;
            layoutParams.height = width;
        }
        view.addView(mPreview);

        if (isRotated) {
            mPreview.findViewById(R.id.rot0).setVisibility(View.GONE);
            final View rot90 = mPreview.findViewById(R.id.rot90);
        } else {
            mPreview.findViewById(R.id.rot90).setVisibility(View.GONE);
            final View rot0 = mPreview.findViewById(R.id.rot0);
        }
    }

    private void notifyChanged() {
        mPreview.onTuningChanged(NAV_BAR_VIEWS, mNavBarAdapter.getNavString());
=======
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mHandler = new Handler();
        super.onCreate(savedInstanceState);
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.nav_bar_tuner);
        bindLayout((ListPreference) findPreference(LAYOUT));
        bindButton(NAV_BAR_LEFT, NAVSPACE, LEFT);
        bindButton(NAV_BAR_RIGHT, MENU_IME, RIGHT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTunables.forEach(t -> Dependency.get(TunerService.class).removeTunable(t));
    }

    private void addTunable(Tunable tunable, String... keys) {
        mTunables.add(tunable);
        Dependency.get(TunerService.class).addTunable(tunable, keys);
    }

    private void bindLayout(ListPreference preference) {
        addTunable((key, newValue) -> mHandler.post(() -> {
            String val = newValue;
            if (val == null) {
                val = "default";
            }
            preference.setValue(val);
        }), NAV_BAR_VIEWS);
        preference.setOnPreferenceChangeListener((preference1, newValue) -> {
            String val = (String) newValue;
            if ("default".equals(val)) val = null;
            Dependency.get(TunerService.class).setValue(NAV_BAR_VIEWS, val);
            return true;
        });
    }

    private void bindButton(String setting, String def, String k) {
        ListPreference type = (ListPreference) findPreference(TYPE + "_" + k);
        Preference keycode = findPreference(KEYCODE + "_" + k);
        ListPreference icon = (ListPreference) findPreference(ICON + "_" + k);
        setupIcons(icon);
        addTunable((key, newValue) -> mHandler.post(() -> {
            String val = newValue;
            if (val == null) {
                val = def;
            }
            String button = extractButton(val);
            if (button.startsWith(KEY)) {
                type.setValue(KEY);
                String uri = extractImage(button);
                int code = extractKeycode(button);
                icon.setValue(uri);
                updateSummary(icon);
                keycode.setSummary(code + "");
                keycode.setVisible(true);
                icon.setVisible(true);
            } else {
                type.setValue(button);
                keycode.setVisible(false);
                icon.setVisible(false);
            }
<<<<<<< HEAD
            return true;
        } else if (item.getItemId() == RESET) {
            Settings.Secure.putString(getContext().getContentResolver(),
                    NAV_BAR_VIEWS, null);
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static CharSequence getLabel(String button, Context context) {
        if (button.startsWith(HOME)) {
            return context.getString(R.string.accessibility_home);
        } else if (button.startsWith(BACK)) {
            return context.getString(R.string.accessibility_back);
        } else if (button.startsWith(RECENT)) {
            return context.getString(R.string.accessibility_recent);
        } else if (button.startsWith(NAVSPACE)) {
            return context.getString(R.string.space);
        } else if (button.equals(MENU_IME)) {
            return context.getString(R.string.menu_ime);
        } else if (button.equals(MENU_IME_ALWAYS_SHOW)) {
            return context.getString(R.string.menu_ime_always_show);
        } else if (button.startsWith(SEARCH)) {
            return context.getString(R.string.search);
        } else if (button.startsWith(CLIPBOARD)) {
            return context.getString(R.string.clipboard);
        } else if (button.startsWith(KEY)) {
            return context.getString(R.string.keycode);
        }
        return button;
    }

    private static class Holder extends RecyclerView.ViewHolder {
        private TextView title;

        public Holder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.title);
        }
    }

    private static class Dividers extends RecyclerView.ItemDecoration {
        private final Drawable mDivider;

        public Dividers(Context context) {
            TypedValue value = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.listDivider, value, true);
            mDivider = context.getDrawable(value.resourceId);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

    private void selectImage() {
        startActivityForResult(KeycodeSelectionHelper.getSelectImageIntent(), READ_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == READ_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            final Uri uri = data.getData();
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
            mNavBarAdapter.onImageSelected(uri);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class NavBarAdapter extends RecyclerView.Adapter<Holder>
            implements View.OnClickListener {

        private static final String START = "start";
        private static final String CENTER = "center";
        private static final String END = "end";
        private static final String ADD = "add";

        private static final int ADD_ID = 0;
        private static final int BUTTON_ID = 1;
        private static final int CATEGORY_ID = 2;

        private List<String> mButtons = new ArrayList<>();
        private List<CharSequence> mLabels = new ArrayList<>();
        private int mCategoryLayout;
        private int mButtonLayout;
        private ItemTouchHelper mTouchHelper;

        // Stored keycode while we wait for image selection on a KEY.
        private int mKeycode;

        public NavBarAdapter(Context context) {
            TypedArray attrs = context.getTheme().obtainStyledAttributes(null,
                    android.R.styleable.Preference, android.R.attr.preferenceStyle, 0);
            mButtonLayout = attrs.getResourceId(android.R.styleable.Preference_layout, 0);
            attrs = context.getTheme().obtainStyledAttributes(null,
                    android.R.styleable.Preference, android.R.attr.preferenceCategoryStyle, 0);
            mCategoryLayout = attrs.getResourceId(android.R.styleable.Preference_layout, 0);
        }

        public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
            mTouchHelper = itemTouchHelper;
        }

        public void clear() {
            mButtons.clear();
            mLabels.clear();
            notifyDataSetChanged();
        }

        public void addButton(String button, CharSequence label) {
            mButtons.add(button);
            mLabels.add(label);
            notifyItemInserted(mLabels.size() - 1);
            notifyChanged();
        }

        public boolean hasHomeButton() {
            final int N = mButtons.size();
            for (int i = 0; i < N; i++) {
                if (mButtons.get(i).startsWith(HOME)) {
                    return true;
                }
            }
            return false;
        }

        public String getNavString() {
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < mButtons.size() - 1; i++) {
                String button = mButtons.get(i);
                if (button.equals(CENTER) || button.equals(END)) {
                    if (builder.length() == 0 || builder.toString().endsWith(GRAVITY_SEPARATOR)) {
                        // No start or center buttons, fill with a space.
                        builder.append(NAVSPACE);
                    }
                    builder.append(GRAVITY_SEPARATOR);
                    continue;
                } else if (builder.length() != 0 && !builder.toString().endsWith(
                        GRAVITY_SEPARATOR)) {
                    builder.append(BUTTON_SEPARATOR);
                }
                builder.append(button);
            }
            if (builder.toString().endsWith(GRAVITY_SEPARATOR)) {
                // No end buttons, fill with space.
                builder.append(NAVSPACE);
            }
            return builder.toString();
        }

        @Override
        public int getItemViewType(int position) {
            String button = mButtons.get(position);
            if (button.equals(START) || button.equals(CENTER) || button.equals(END)) {
                return CATEGORY_ID;
            }
            if (button.equals(ADD)) {
                return ADD_ID;
            }
            return BUTTON_ID;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            final Context context = parent.getContext();
            final LayoutInflater inflater = LayoutInflater.from(context);
            final View view = inflater.inflate(getLayoutId(viewType), parent, false);
            if (viewType == BUTTON_ID) {
                inflater.inflate(R.layout.nav_control_widget,
                        (ViewGroup) view.findViewById(android.R.id.widget_frame));
            }
            return new Holder(view);
        }

        private int getLayoutId(int viewType) {
            if (viewType == CATEGORY_ID) {
                return mCategoryLayout;
            }
            return mButtonLayout;
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            holder.title.setText(mLabels.get(position));
            if (holder.getItemViewType() == BUTTON_ID) {
                bindButton(holder, position);
            } else if (holder.getItemViewType() == ADD_ID) {
                bindAdd(holder);
            }
        }

        private void bindAdd(Holder holder) {
            TypedValue value = new TypedValue();
            final Context context = holder.itemView.getContext();
            context.getTheme().resolveAttribute(android.R.attr.colorAccent, value, true);
            final ImageView icon = (ImageView) holder.itemView.findViewById(android.R.id.icon);
            icon.setImageResource(R.drawable.ic_add);
            icon.setImageTintList(ColorStateList.valueOf(context.getColor(value.resourceId)));
            holder.itemView.findViewById(android.R.id.summary).setVisibility(View.GONE);
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddDialog(v.getContext());
                }
            });
        }

        private void bindButton(final Holder holder, int position) {
            holder.itemView.findViewById(android.R.id.icon_frame).setVisibility(View.GONE);
            holder.itemView.findViewById(android.R.id.summary).setVisibility(View.GONE);
            bindClick(holder.itemView.findViewById(R.id.close), holder);
            bindClick(holder.itemView.findViewById(R.id.width), holder);
            holder.itemView.findViewById(R.id.drag).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mTouchHelper.startDrag(holder);
                    return true;
                }
            });
        }

        private void showAddDialog(final Context context) {
            final String[] options = new String[] {
                    BACK, HOME, RECENT, MENU_IME, MENU_IME_ALWAYS_SHOW, SEARCH, NAVSPACE, CLIPBOARD, KEY,
            };
            final CharSequence[] labels = new CharSequence[options.length];
            for (int i = 0; i < options.length; i++) {
                labels[i] = getLabel(options[i], context);
            }
            new AlertDialog.Builder(context)
                    .setTitle(R.string.select_button)
                    .setItems(labels, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (KEY.equals(options[which])) {
                                showKeyDialogs(context);
                            } else {
                                int index = mButtons.size() - 1;
                                showAddedMessage(context, options[which]);
                                mButtons.add(index, options[which]);
                                mLabels.add(index, labels[which]);

                                notifyItemInserted(index);
                                notifyChanged();
                            }
                        }
                    }).setNegativeButton(android.R.string.cancel, null)
                    .show();
        }

        private void onImageSelected(Uri uri) {
            int index = mButtons.size() - 1;
            mButtons.add(index, KEY + KEY_CODE_START + mKeycode + KEY_IMAGE_DELIM + uri.toString()
                    + KEY_CODE_END);
            mLabels.add(index, getLabel(KEY, getContext()));

            notifyItemInserted(index);
            notifyChanged();
        }

        private void showKeyDialogs(final Context context) {
            final KeycodeSelectionHelper.OnSelectionComplete listener =
                    new KeycodeSelectionHelper.OnSelectionComplete() {
                        @Override
                        public void onSelectionComplete(int code) {
                            mKeycode = code;
                            selectImage();
                        }
                    };
            new AlertDialog.Builder(context)
                    .setTitle(R.string.keycode)
                    .setMessage(R.string.keycode_description)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            KeycodeSelectionHelper.showKeycodeSelect(context, listener);
                        }
                    }).show();
        }

        private void showAddedMessage(Context context, String button) {
            if (CLIPBOARD.equals(button)) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.clipboard)
                        .setMessage(R.string.clipboard_description)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        }

        private void bindClick(View view, Holder holder) {
            view.setOnClickListener(this);
            view.setTag(holder);
        }

        @Override
        public void onClick(View v) {
            Holder holder = (Holder) v.getTag();
            if (v.getId() == R.id.width) {
                showWidthDialog(holder, v.getContext());
            } else if (v.getId() == R.id.close) {
                int position = holder.getAdapterPosition();
                mButtons.remove(position);
                mLabels.remove(position);
                notifyItemRemoved(position);
                notifyChanged();
            }
        }

        private void showWidthDialog(final Holder holder, Context context) {
            final String buttonSpec = mButtons.get(holder.getAdapterPosition());
            float amount = extractSize(buttonSpec);
            final AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(R.string.adjust_button_width)
                    .setView(R.layout.nav_width_view)
                    .setNegativeButton(android.R.string.cancel, null).create();
            dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    context.getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int which) {
                            final String button = extractButton(buttonSpec);
                            SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.seekbar);
                            if (seekBar.getProgress() == 75) {
                                mButtons.set(holder.getAdapterPosition(), button);
                            } else {
                                float amount = (seekBar.getProgress() + 25) / 100f;
                                mButtons.set(holder.getAdapterPosition(), button
                                        + SIZE_MOD_START + amount + SIZE_MOD_END);
                            }
                            notifyChanged();
                        }
                    });
            dialog.show();
            SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.seekbar);
            // Range is .25 - 1.75.
            seekBar.setMax(150);
            seekBar.setProgress((int) ((amount - .25f) * 100));
        }

        @Override
        public int getItemCount() {
            return mButtons.size();
        }

        private final ItemTouchHelper.Callback mCallbacks = new ItemTouchHelper.Callback() {
            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView,
                    RecyclerView.ViewHolder viewHolder) {
                if (viewHolder.getItemViewType() != BUTTON_ID) {
                    return makeMovementFlags(0, 0);
                }
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                    RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                if (to == 0) {
                    // Can't go above the top.
                    return false;
                }
                move(from, to, mButtons);
                move(from, to, mLabels);
                notifyChanged();
                notifyItemMoved(from, to);
                return true;
            }

            private <T> void move(int from, int to, List<T> list) {
                list.add(from > to ? to : to + 1, list.get(from));
                list.remove(from > to ? from + 1 : from);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Don't care.
            }
=======
        }), setting);
        OnPreferenceChangeListener listener = (preference, newValue) -> {
            mHandler.post(() -> {
                setValue(setting, type, keycode, icon);
                updateSummary(icon);
            });
            return true;
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
        };
        type.setOnPreferenceChangeListener(listener);
        icon.setOnPreferenceChangeListener(listener);
        keycode.setOnPreferenceClickListener(preference -> {
            EditText editText = new EditText(getContext());
            new AlertDialog.Builder(getContext())
                    .setTitle(preference.getTitle())
                    .setView(editText)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        int code = KeyEvent.KEYCODE_ENTER;
                        try {
                            code = Integer.parseInt(editText.getText().toString());
                        } catch (Exception e) {
                        }
                        keycode.setSummary(code + "");
                        setValue(setting, type, keycode, icon);
                    }).show();
            return true;
        });
    }

    private void updateSummary(ListPreference icon) {
        try {
            int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14,
                    getContext().getResources().getDisplayMetrics());
            String pkg = icon.getValue().split("/")[0];
            int id = Integer.parseInt(icon.getValue().split("/")[1]);
            SpannableStringBuilder builder = new SpannableStringBuilder();
            Drawable d = Icon.createWithResource(pkg, id)
                    .loadDrawable(getContext());
            d.setTint(Color.BLACK);
            d.setBounds(0, 0, size, size);
            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
            builder.append("  ", span, 0);
            builder.append(" ");
            for (int i = 0; i < ICONS.length; i++) {
                if (ICONS[i][0] == id) {
                    builder.append(getString(ICONS[i][1]));
                }
            }
            icon.setSummary(builder);
        } catch (Exception e) {
            Log.d("NavButton", "Problem with summary", e);
            icon.setSummary(null);
        }
    }

    private void setValue(String setting, ListPreference type, Preference keycode,
            ListPreference icon) {
        String button = type.getValue();
        if (KEY.equals(button)) {
            String uri = icon.getValue();
            int code = KeyEvent.KEYCODE_ENTER;
            try {
                code = Integer.parseInt(keycode.getSummary().toString());
            } catch (Exception e) {
            }
            button = button + KEY_CODE_START + code + KEY_IMAGE_DELIM + uri + KEY_CODE_END;
        }
        Dependency.get(TunerService.class).setValue(setting, button);
    }

    private void setupIcons(ListPreference icon) {
        CharSequence[] labels = new CharSequence[ICONS.length];
        CharSequence[] values = new CharSequence[ICONS.length];
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14,
                getContext().getResources().getDisplayMetrics());
        for (int i = 0; i < ICONS.length; i++) {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            Drawable d = Icon.createWithResource(getContext().getPackageName(), ICONS[i][0])
                    .loadDrawable(getContext());
            d.setTint(Color.BLACK);
            d.setBounds(0, 0, size, size);
            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
            builder.append("  ", span, 0);
            builder.append(" ");
            builder.append(getString(ICONS[i][1]));
            labels[i] = builder;
            values[i] = getContext().getPackageName() + "/" + ICONS[i][0];
        }
        icon.setEntries(labels);
        icon.setEntryValues(values);
    }
}
