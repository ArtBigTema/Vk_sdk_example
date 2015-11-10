package lb.listviewvariants.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vk.vktestapp.R;


public class CircularContactView extends ViewSwitcher {
    //  private static final int DEFAULT_CONTENT_SIZE_IN_DP=20;
    private ImageView mImageView;
    private TextView mTextView;
    private Bitmap mBitmap;
    private CharSequence mText;
    private int mBackgroundColor = 0, mImageResId = 0;
    private int mContentSize;
    private Target loadtarget;

    public CircularContactView(final Context context) {
        this(context, null);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public CircularContactView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        addView(mImageView = new ImageView(context), new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT, Gravity.CENTER));
        addView(mTextView = new TextView(context), new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT, Gravity.CENTER));
        mTextView.setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            mTextView.setAllCaps(true);
        mContentSize = getResources().getDimensionPixelSize(R.dimen.list_item__contact_imageview_size);
        if (isInEditMode())
            setTextAndBackgroundColor("", 0xFFff0000);
    }

    public void setContentSize(final int contentSize) {
        this.mContentSize = contentSize;
    }

    @SuppressWarnings("deprecation")
    private void drawContent(final int viewWidth, final int viewHeight) {
        ShapeDrawable roundedBackgroundDrawable = null;
        if (mBackgroundColor != 0) {
            roundedBackgroundDrawable = new ShapeDrawable(new OvalShape());
            roundedBackgroundDrawable.getPaint().setColor(mBackgroundColor);
            roundedBackgroundDrawable.setIntrinsicHeight(viewHeight);
            roundedBackgroundDrawable.setIntrinsicWidth(viewWidth);
            roundedBackgroundDrawable.setBounds(new Rect(0, 0, viewWidth, viewHeight));
        }
        if (mImageResId != 0) {
            mImageView.setBackgroundDrawable(roundedBackgroundDrawable);
            mImageView.setImageResource(mImageResId);
            mImageView.setScaleType(ScaleType.CENTER_INSIDE);
        } else if (mText != null) {
            mTextView.setText(mText);
            mTextView.setBackgroundDrawable(roundedBackgroundDrawable);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, viewHeight / 2);
        } else if (mBitmap != null) {
            mImageView.setScaleType(ScaleType.FIT_CENTER);
            mImageView.setBackgroundDrawable(roundedBackgroundDrawable);
            if (mBitmap.getWidth() != mBitmap.getHeight())
                mBitmap = ThumbnailUtils.extractThumbnail(mBitmap, viewWidth, viewHeight);
            final RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(),
                    mBitmap);
            roundedBitmapDrawable.setCornerRadius((mBitmap.getWidth() + mBitmap.getHeight()) / 4);
            mImageView.setImageDrawable(roundedBitmapDrawable);
        }
        resetValuesState(false);
    }

    public void setTextAndBackgroundColor(final CharSequence text, final int backgroundColor) {
        resetValuesState(true);
        while (getCurrentView() != mTextView)
            showNext();
        this.mBackgroundColor = backgroundColor;
        mText = text;
        drawContent(mContentSize, mContentSize);
    }

    public void setImageResource(final int imageResId, final int backgroundColor) {
        resetValuesState(true);
        while (getCurrentView() != mImageView)
            showNext();
        mImageResId = imageResId;
        this.mBackgroundColor = backgroundColor;
        drawContent(mContentSize, mContentSize);
    }

    public void setImageBitmap(final Bitmap bitmap) {
        setImageBitmapAndBackgroundColor(bitmap, 0);
    }

    public void setImageBitmapAndBackgroundColor(final Bitmap bitmap, final int backgroundColor) {
        resetValuesState(true);
        while (getCurrentView() != mImageView)
            showNext();
        this.mBackgroundColor = backgroundColor;
        mBitmap = bitmap;
        drawContent(mContentSize, mContentSize);
    }

    private void resetValuesState(final boolean alsoResetViews) {
        mBackgroundColor = mImageResId = 0;
        mBitmap = null;
        mText = null;
        if (alsoResetViews) {
            mTextView.setText(null);
            mTextView.setBackgroundDrawable(null);
            mImageView.setImageBitmap(null);
            mImageView.setBackgroundDrawable(null);
        }
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public TextView getTextView() {
        return mTextView;
    }

    public void loadBitmap(Context context, String url) {
        if (loadtarget == null) loadtarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                setImageBitmap(bitmap);
                mBitmap = bitmap;
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        Picasso.with(context).load(url).into(loadtarget);
    }
}
