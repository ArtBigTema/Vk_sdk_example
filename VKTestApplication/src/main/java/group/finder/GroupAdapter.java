package group.finder;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vk.Sub.Contact;
import com.vk.vktestapp.FriendLVActivity;
import com.vk.vktestapp.R;

import java.util.List;
import java.util.Locale;

import lb.listviewvariants.utils.CircularContactView;
import lb.listviewvariants.utils.ContactImageUtil;
import lb.listviewvariants.utils.ImageCache;
import lb.listviewvariants.utils.async_task_thread_pool.AsyncTaskEx;
import lb.listviewvariants.utils.async_task_thread_pool.AsyncTaskThreadPool;

/**
 * Created by Артем on 06.12.2015.
 */
public class GroupAdapter extends ArrayAdapter<Contact> {
    private List<Contact> mContacts;
    private Context context;
    private final int CONTACT_PHOTO_IMAGE_SIZE;
    private final int[] PHOTO_TEXT_BACKGROUND_COLORS;
    private final AsyncTaskThreadPool mAsyncTaskThreadPool = new AsyncTaskThreadPool(1, 2, 10);

    public GroupAdapter(Context context, int resource, List<Contact> objects) {
        super(context, resource, objects);
        this.context = context;
        mContacts = objects;
        PHOTO_TEXT_BACKGROUND_COLORS = context.getResources().getIntArray(R.array.contacts_text_background_colors);
        CONTACT_PHOTO_IMAGE_SIZE = context.getResources().getDimensionPixelSize(
                R.dimen.list_item__contact_imageview_size);
    }


    @Override
    public int getCount() {
        return mContacts.size();
    }

    @Override
    public Contact getItem(int position) {
        return mContacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mContacts.get(position).getContactId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final FriendLVActivity.ViewHolder holder;
        final View rootView;
        if (convertView == null) {
            holder = new FriendLVActivity.ViewHolder();
            rootView = LayoutInflater.from(context).inflate(R.layout.listview_item, parent, false);
            holder.friendProfileCircularContactView = (CircularContactView) rootView
                    .findViewById(R.id.listview_item__friendPhotoImageView);
            holder.onlineImage = (ImageView) rootView
                    .findViewById(R.id.online);
            holder.friendProfileCircularContactView.getTextView().setTextColor(0xFFffffff);
            holder.friendName = (TextView) rootView
                    .findViewById(R.id.listview_item__friendNameTextView);
            holder.headerView = (TextView) rootView.findViewById(R.id.header_text);
         //   holder.headerView.setVisibility(View.GONE);
            rootView.findViewById(R.id.fl_lv_item).setVisibility(View.GONE);
            rootView.setTag(holder);
        } else {
            rootView = convertView;
            holder = (FriendLVActivity.ViewHolder) rootView.getTag();
        }
        final Contact contact = getItem(position);
        final String displayName = contact.getDisplayName();
        holder.friendName.setText(displayName);

        if (contact.isOnline()) {
            if (contact.isMonline()) {
                holder.onlineImage.setImageResource(R.drawable.phone);
            } else {
                holder.onlineImage.setImageResource(R.drawable.co);
            }
        } else {
            holder.onlineImage.setImageResource(0);
        }

        boolean hasPhoto = !TextUtils.isEmpty(contact.getPhotoId());
        if (holder.updateTask != null && !holder.updateTask.isCancelled())
            holder.updateTask.cancel(true);
        final Bitmap cachedBitmap = hasPhoto ? ImageCache.INSTANCE.getBitmapFromMemCache(contact.getPhotoId()) : null;
        if (!contact.image.contains("deact")) {
            holder.friendProfileCircularContactView.loadBitmap(context, contact.image);
        } else {
            final int backgroundColorToUse = PHOTO_TEXT_BACKGROUND_COLORS[position
                    % PHOTO_TEXT_BACKGROUND_COLORS.length];
            if (TextUtils.isEmpty(displayName))
                holder.friendProfileCircularContactView.setImageResource(R.drawable.ic_person_white_120dp,
                        backgroundColorToUse);
            else {
                final String characterToShow = TextUtils.isEmpty(displayName) ? "" : displayName.substring(0, 1).toUpperCase(Locale.getDefault());
                holder.friendProfileCircularContactView.setTextAndBackgroundColor(characterToShow, backgroundColorToUse);
            }
            if (hasPhoto) {
                holder.updateTask = new AsyncTaskEx<Void, Void, Bitmap>() {

                    @Override
                    public Bitmap doInBackground(final Void... params) {
                        if (isCancelled())
                            return null;
                        final Bitmap b = ContactImageUtil.loadContactPhotoThumbnail(context, contact.getPhotoId(), CONTACT_PHOTO_IMAGE_SIZE);
                        if (b != null)
                            return ThumbnailUtils.extractThumbnail(b, CONTACT_PHOTO_IMAGE_SIZE,
                                    CONTACT_PHOTO_IMAGE_SIZE);
                        return null;
                    }

                    @Override
                    public void onPostExecute(final Bitmap result) {
                        super.onPostExecute(result);
                        if (result == null)
                            return;
                        ImageCache.INSTANCE.addBitmapToCache(contact.getPhotoId(), result);
                        holder.friendProfileCircularContactView.setImageBitmap(result);
                    }
                };
                mAsyncTaskThreadPool.executeAsyncTask(holder.updateTask);
            }
        }

       // bindSectionHeader(holder.headerView, null, position);
        return rootView;
    }
}
