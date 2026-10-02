/*
 * Copyright (C) 2013 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.keyboard.emoji;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import helium314.keyboard.event.HapticEvent;
import helium314.keyboard.keyboard.Key;
import helium314.keyboard.keyboard.KeyboardActionListener;
import helium314.keyboard.keyboard.KeyboardLayoutSet;
import helium314.keyboard.keyboard.internal.KeyVisualAttributes;
import helium314.keyboard.keyboard.internal.keyboard_parser.EmojiParserKt;
import helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyCode;
import helium314.keyboard.latin.AudioAndHapticFeedbackManager;
import helium314.keyboard.latin.dictionary.Dictionary;
import helium314.keyboard.latin.dictionary.DictionaryFactory;
import helium314.keyboard.latin.R;
import helium314.keyboard.latin.RichInputMethodManager;
import helium314.keyboard.latin.RichInputMethodSubtype;
import helium314.keyboard.latin.SingleDictionaryFacilitator;
import helium314.keyboard.latin.common.ColorType;
import helium314.keyboard.latin.common.Colors;
import helium314.keyboard.latin.settings.Settings;
import helium314.keyboard.latin.settings.SettingsValues;
import helium314.keyboard.latin.utils.DictionaryInfoUtils;
import helium314.keyboard.latin.utils.ResourceUtils;

import static helium314.keyboard.latin.common.Constants.NOT_A_COORDINATE;

/**
 * View class to implement Emoji palettes, laid out like the iOS emoji keyboard (LiBoard):
 * <ol>
 * <li> Search field, opening the emoji search.
 * <li> Title of the category currently in view.
 * <li> One horizontally scrolling band with the pages of all categories in a row.
 * <li> Bottom bar: ABC (back to the main keyboard), category tabs, delete.
 * </ol>
 * Because of the above reasons, this class doesn't extend {@link KeyboardView}.
 */
public final class EmojiPalettesView extends LinearLayout
        implements View.OnClickListener, EmojiViewCallback {
    private static final int DELETE_REPEAT_START_DELAY = 400;
    private static final int DELETE_REPEAT_INTERVAL = 50;

    /** One page of one category in the emoji band. */
    private record PageRef(EmojiCategory.Category category, int page) {}

    private static final class PageViewHolder extends RecyclerView.ViewHolder {
        private final EmojiPageKeyboardView mKeyboardView;

        private PageViewHolder(EmojiPageKeyboardView view) {
            super(view);
            mKeyboardView = view;
        }
    }

    /** All pages of all shown categories, side by side. */
    private final class BandAdapter extends RecyclerView.Adapter<PageViewHolder> {
        @NonNull
        @Override
        public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final EmojiPageKeyboardView view = (EmojiPageKeyboardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.emoji_keyboard_page, parent, false);
            view.setEmojiViewCallback(EmojiPalettesView.this);
            return new PageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
            final PageRef ref = mPages.get(position);
            holder.mKeyboardView.setKeyboard(mEmojiCategory.getKeyboardFromAdapterPosition(ref.category(), ref.page()));
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull PageViewHolder holder) {
            holder.mKeyboardView.releaseCurrentKey(false);
            holder.mKeyboardView.deallocateMemory();
        }

        @Override
        public int getItemCount() {
            return mPages.size();
        }
    }

    private static SingleDictionaryFacilitator sDictionaryFacilitator;

    private boolean initialized = false;
    private final Colors mColors;
    private final EmojiLayoutParams mEmojiLayoutParams;
    private KeyboardActionListener mKeyboardActionListener = KeyboardActionListener.EMPTY_LISTENER;
    private final EmojiCategory mEmojiCategory;
    private final List<PageRef> mPages = new ArrayList<>();
    private RecyclerView mBand;
    private LinearLayoutManager mBandLayoutManager;
    private TextView mSearchField;
    private TextView mCategoryTitle;
    private LinearLayout mTabs;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mDeleteRepeat = new Runnable() {
        @Override
        public void run() {
            mKeyboardActionListener.onPressKey(KeyCode.DELETE, 1, 1, HapticEvent.KEY_REPEAT);
            mKeyboardActionListener.onCodeInput(KeyCode.DELETE, NOT_A_COORDINATE, NOT_A_COORDINATE, true);
            mHandler.postDelayed(this, DELETE_REPEAT_INTERVAL);
        }
    };

    public EmojiPalettesView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.emojiPalettesViewStyle);
    }

    public EmojiPalettesView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mColors = Settings.getValues().mColors;
        final KeyboardLayoutSet.Builder builder = new KeyboardLayoutSet.Builder(context, null);
        final Resources res = context.getResources();
        mEmojiLayoutParams = new EmojiLayoutParams(res);
        builder.setSubtype(RichInputMethodSubtype.Companion.getEmojiSubtype());
        builder.setKeyboardGeometry(ResourceUtils.getKeyboardWidth(context, Settings.getValues()),
                mEmojiLayoutParams.getEmojiKeyboardHeight());
        final KeyboardLayoutSet layoutSet = builder.build();
        final TypedArray emojiPalettesViewAttr = context.obtainStyledAttributes(attrs,
                R.styleable.EmojiPalettesView, defStyle, R.style.EmojiPalettesView);
        mEmojiCategory = new EmojiCategory(context, layoutSet, emojiPalettesViewAttr);
        mEmojiCategory.setGridHeight(mEmojiLayoutParams.getEmojiKeyboardHeight());
        emojiPalettesViewAttr.recycle();
        setFitsSystemWindows(true);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int width = ResourceUtils.getKeyboardWidth(getContext(), Settings.getValues())
                + getPaddingLeft() + getPaddingRight();
        final int height = new EmojiLayoutParams(getResources()).getTotalHeight()
                + getPaddingTop() + getPaddingBottom();
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    private void addTab(LinearLayout host, EmojiCategory.Category category) {
        final ImageView iconView = new ImageView(getContext());
        mColors.setColor(iconView, ColorType.EMOJI_CATEGORY);
        iconView.setScaleType(ImageView.ScaleType.CENTER);
        iconView.setImageResource(mEmojiCategory.getCategoryTabIcon(category));
        iconView.setContentDescription(mEmojiCategory.getAccessibilityDescription(category));
        iconView.setTag(category);
        host.addView(iconView);
        iconView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        iconView.setOnClickListener(this);
    }

    private GradientDrawable roundedBackground(int color, float radiusDp) {
        final GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radiusDp * getResources().getDisplayMetrics().density);
        return d;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initialize() {
        if (initialized) return;
        mEmojiCategory.initialize();
        mPages.clear();
        for (EmojiCategory.CategoryProperties properties : mEmojiCategory.getShownCategories()) {
            for (int i = 0; i < properties.getPageCount(); i++)
                mPages.add(new PageRef(properties.getCategory(), i));
        }

        mSearchField = findViewById(R.id.emoji_search_field);
        mCategoryTitle = findViewById(R.id.emoji_category_title);
        mTabs = findViewById(R.id.emoji_tabs);
        mBand = findViewById(R.id.emoji_strip);
        mEmojiLayoutParams.setHeight(mSearchField, mEmojiLayoutParams.getSearchFieldHeight());
        mEmojiLayoutParams.setHeight(mCategoryTitle, mEmojiLayoutParams.getTitleHeight());
        mEmojiLayoutParams.setHeight(mBand, mEmojiLayoutParams.getEmojiKeyboardHeight());
        mEmojiLayoutParams.setHeight(findViewById(R.id.emoji_bottom_bar), mEmojiLayoutParams.getBottomRowKeyboardHeight());

        // search field
        final int keyText = mColors.get(ColorType.KEY_TEXT);
        final int hintText = mColors.get(ColorType.KEY_HINT_TEXT);
        mSearchField.setBackground(roundedBackground(mColors.get(ColorType.KEY_BACKGROUND), 10));
        mSearchField.setTextColor(hintText);
        final android.graphics.drawable.Drawable searchIcon = getContext().getDrawable(R.drawable.sym_keyboard_search_rounded);
        if (searchIcon != null) {
            final int size = (int) (18 * getResources().getDisplayMetrics().density);
            searchIcon.setBounds(0, 0, size, size);
            searchIcon.setTint(hintText);
            mSearchField.setCompoundDrawablesRelative(searchIcon, null, null, null);
        }
        mSearchField.setOnClickListener(v -> {
            AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(KeyCode.NOT_SPECIFIED, this, HapticEvent.KEY_PRESS);
            mKeyboardActionListener.onCodeInput(KeyCode.EMOJI_SEARCH, NOT_A_COORDINATE, NOT_A_COORDINATE, false);
        });
        mCategoryTitle.setTextColor(hintText);

        // emoji band
        mBandLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBand.setLayoutManager(mBandLayoutManager);
        mBand.setItemViewCacheSize(4);
        mBand.setAdapter(new BandAdapter());
        mBand.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                updateCategoryFromScroll();
            }
        });

        // bottom bar
        final TextView abc = findViewById(R.id.emoji_abc);
        abc.setTextColor(keyText);
        abc.setOnClickListener(v -> {
            AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(KeyCode.NOT_SPECIFIED, this, HapticEvent.KEY_PRESS);
            mKeyboardActionListener.onCodeInput(KeyCode.ALPHA, NOT_A_COORDINATE, NOT_A_COORDINATE, false);
        });
        for (EmojiCategory.CategoryProperties properties : mEmojiCategory.getShownCategories()) {
            addTab(mTabs, properties.getCategory());
        }
        final ImageView delete = findViewById(R.id.emoji_delete);
        delete.setImageResource(R.drawable.sym_keyboard_delete_rounded);
        mColors.setColor(delete, ColorType.KEY_ICON);
        delete.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN -> {
                    v.setPressed(true);
                    mKeyboardActionListener.onPressKey(KeyCode.DELETE, 0, 1, HapticEvent.KEY_PRESS);
                    mKeyboardActionListener.onCodeInput(KeyCode.DELETE, NOT_A_COORDINATE, NOT_A_COORDINATE, false);
                    mHandler.postDelayed(mDeleteRepeat, DELETE_REPEAT_START_DELAY);
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.setPressed(false);
                    mHandler.removeCallbacks(mDeleteRepeat);
                    mKeyboardActionListener.onReleaseKey(KeyCode.DELETE, false);
                }
            }
            return true;
        });

        setCurrentCategory(mEmojiCategory.getCurrentCategory(), true);
        initialized = true;
    }

    private int firstPageOf(EmojiCategory.Category category) {
        for (int i = 0; i < mPages.size(); i++) {
            if (mPages.get(i).category() == category) return i;
        }
        return 0;
    }

    private void updateCategoryFromScroll() {
        final int position = mBandLayoutManager.findFirstVisibleItemPosition();
        if (position == RecyclerView.NO_POSITION) return;
        // the category whose page covers the left half of the band is the current one
        final View first = mBandLayoutManager.findViewByPosition(position);
        final int shown = (first != null && first.getRight() < mBand.getWidth() / 2 && position + 1 < mPages.size())
                ? position + 1 : position;
        final PageRef ref = mPages.get(shown);
        if (ref.category() != mEmojiCategory.getCurrentCategory()) {
            if (mEmojiCategory.isInRecentTab()) getRecentsKeyboard().flushPendingRecentKeys();
            showCategory(ref.category());
        }
        mEmojiCategory.setCurrentCategoryPageId(ref.page());
    }

    /**
     * Called from {@link EmojiPageKeyboardView} through {@link android.view.View.OnClickListener}
     * interface to handle non-canceled touch-up events from View-based elements such as the space
     * bar.
     */
    @Override
    public void onClick(View v) {
        final Object tag = v.getTag();
        if (tag instanceof EmojiCategory.Category category) {
            AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(KeyCode.NOT_SPECIFIED, this, HapticEvent.KEY_PRESS);
            setCurrentCategory(category, true);
        }
    }

    /**
     * Called from {@link EmojiPageKeyboardView} through {@link EmojiViewCallback}
     * interface to handle touch events from non-View-based elements such as Emoji buttons.
     */
    @Override
    public void onPressKey(final Key key) {
        final int code = key.getCode();
        mKeyboardActionListener.onPressKey(code, 0, 1, HapticEvent.KEY_PRESS);
    }

    /**
     * Called from {@link EmojiPageKeyboardView} through {@link EmojiViewCallback}
     * interface to handle touch events from non-View-based elements such as Emoji buttons.
     * This may be called without any prior call to {@link EmojiViewCallback#onPressKey(Key)}.
     */
    @Override
    public void onReleaseKey(final Key key) {
        addRecentKey(key);
        final int code = key.getCode();
        if (code == KeyCode.MULTIPLE_CODE_POINTS) {
            // todo: when we enter some emoticons, e.g. :-D, inline emoji search is triggered and emoji view is closed
            //  (one more instance of "emoji search should recognize emoticons", but in this case could be fixed in other ways)
            mKeyboardActionListener.onTextInput(key.getOutputText());
        } else {
            mKeyboardActionListener.onCodeInput(code, NOT_A_COORDINATE, NOT_A_COORDINATE, false);
        }
        mKeyboardActionListener.onReleaseKey(code, false);
        if (Settings.getValues().mAlphaAfterEmojiInEmojiView)
            mKeyboardActionListener.onCodeInput(KeyCode.ALPHA, NOT_A_COORDINATE, NOT_A_COORDINATE, false);
    }

    @Override
    public String getDescription(String emoji) {
        if (sDictionaryFacilitator == null) {
            return null;
        }

        var wordProperty = sDictionaryFacilitator.getWordProperty(EmojiParserKt.getEmojiNeutralVersion(emoji));
        if (wordProperty == null || ! wordProperty.mHasShortcuts) {
            return null;
        }

        return wordProperty.mShortcutTargets.get(0).mWord;
    }

    public void setHardwareAcceleratedDrawingEnabled(final boolean enabled) {
        if (!enabled) return;
        // TODO: Should use LAYER_TYPE_SOFTWARE when hardware acceleration is off?
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public void startEmojiPalettes(final KeyVisualAttributes keyVisualAttr,
               final EditorInfo editorInfo, final KeyboardActionListener keyboardActionListener) {
        mKeyboardActionListener = keyboardActionListener;
        initialize();
        setupSidePadding();
        initDictionaryFacilitator();
    }

    void addRecentKey(final Key key) {
        if (Settings.getValues().mIncognitoModeEnabled) {
            // We do not want to log recent keys while being in incognito
            return;
        }
        if (getVisibility() == VISIBLE && mEmojiCategory.isInRecentTab()) {
            // don't move the recent emojis around while the user looks at them
            getRecentsKeyboard().addPendingKey(key);
            return;
        }
        getRecentsKeyboard().addKeyFirst(key);
        if (initialized)
            mBand.getAdapter().notifyItemChanged(firstPageOf(EmojiCategory.Category.RECENTS));
    }

    private void setupSidePadding() {
        final SettingsValues sv = Settings.getValues();
        final int keyboardWidth = ResourceUtils.getKeyboardWidth(getContext(), sv);
        final TypedArray keyboardAttr = getContext().obtainStyledAttributes(
                null, R.styleable.Keyboard, R.attr.keyboardStyle, R.style.Keyboard);
        final float leftPadding = keyboardAttr.getFraction(R.styleable.Keyboard_keyboardLeftPadding,
                keyboardWidth, keyboardWidth, 0f) * sv.mSidePaddingScale;
        final float rightPadding =  keyboardAttr.getFraction(R.styleable.Keyboard_keyboardRightPadding,
                keyboardWidth, keyboardWidth, 0f) * sv.mSidePaddingScale;
        keyboardAttr.recycle();
        mBand.setPadding((int) leftPadding, mBand.getPaddingTop(), (int) rightPadding, mBand.getPaddingBottom());
    }

    public void stopEmojiPalettes() {
        if (!initialized) return;
        mHandler.removeCallbacks(mDeleteRepeat);
        getRecentsKeyboard().flushPendingRecentKeys();
        mBand.getAdapter().notifyItemChanged(firstPageOf(EmojiCategory.Category.RECENTS));
    }

    private DynamicGridKeyboard getRecentsKeyboard() {
        return mEmojiCategory.getKeyboard(EmojiCategory.Category.RECENTS, 0);
    }

    public void setKeyboardActionListener(final KeyboardActionListener listener) {
        mKeyboardActionListener = listener;
    }

    /** Jump to a category: scroll the band to its first page (or the remembered page on start). */
    private void setCurrentCategory(EmojiCategory.Category category, boolean scroll) {
        if (scroll) {
            int position = firstPageOf(category);
            if (!initialized && category == mEmojiCategory.getCurrentCategory())
                position += mEmojiCategory.getCurrentCategoryPageId();
            mBandLayoutManager.scrollToPositionWithOffset(Math.min(position, mPages.size() - 1), 0);
        }
        showCategory(category);
    }

    /** Update title and tab highlight, without scrolling. */
    private void showCategory(EmojiCategory.Category category) {
        mEmojiCategory.setCurrentCategory(category);
        mCategoryTitle.setText(mEmojiCategory.getAccessibilityDescription(category).toUpperCase(Locale.getDefault()));
        final int selectedBackground = mColors.get(ColorType.FUNCTIONAL_KEY_BACKGROUND);
        for (int i = 0; i < mTabs.getChildCount(); i++) {
            final View tab = mTabs.getChildAt(i);
            if (!(tab instanceof ImageView icon)) continue;
            final boolean selected = tab.getTag() == category;
            if (selected) {
                final GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(selectedBackground);
                final int size = (int) (32 * getResources().getDisplayMetrics().density);
                final android.graphics.drawable.LayerDrawable background =
                        new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[] { circle });
                background.setLayerSize(0, size, size);
                background.setLayerGravity(0, android.view.Gravity.CENTER);
                icon.setBackground(background);
            } else {
                icon.setBackground(null);
            }
            mColors.setColor(icon, selected ? ColorType.EMOJI_CATEGORY_SELECTED : ColorType.EMOJI_CATEGORY);
        }
    }

    public void clearKeyboardCache() {
        if (!initialized) {
            return;
        }

        mEmojiCategory.clearKeyboardCache();
        mEmojiCategory.setGridHeight(new EmojiLayoutParams(getResources()).getEmojiKeyboardHeight());
        mPages.clear();
        for (EmojiCategory.CategoryProperties properties : mEmojiCategory.getShownCategories()) {
            for (int i = 0; i < properties.getPageCount(); i++)
                mPages.add(new PageRef(properties.getCategory(), i));
        }
        mBand.getAdapter().notifyDataSetChanged();
        closeDictionaryFacilitator();
    }

    private void initDictionaryFacilitator() {
        if (Settings.getValues().mShowEmojiDescriptions) {
            var locale = RichInputMethodManager.getInstance().getCurrentSubtype().getLocale();
            if (sDictionaryFacilitator == null || ! sDictionaryFacilitator.isForLocale(locale)) {
                closeDictionaryFacilitator();
                var dictFile = DictionaryInfoUtils.getCachedDictForLocaleAndType(locale, Dictionary.TYPE_EMOJI, getContext());
                var dictionary = dictFile != null? DictionaryFactory.getDictionary(dictFile, locale) : null;
                sDictionaryFacilitator = dictionary != null? new SingleDictionaryFacilitator(dictionary) : null;
            }
        } else {
            closeDictionaryFacilitator();
        }
    }

    public static void closeDictionaryFacilitator() {
        if (sDictionaryFacilitator != null) {
            sDictionaryFacilitator.closeDictionaries();
            sDictionaryFacilitator = null;
        }
    }
}
