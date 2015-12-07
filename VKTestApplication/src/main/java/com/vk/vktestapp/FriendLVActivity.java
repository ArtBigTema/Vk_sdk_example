package com.vk.vktestapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.vk.Sub.Profile;
import com.vk.infographic.InfoGraphicFriendActivity;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import lb.library.PinnedHeaderListView;
import lb.library.SearchablePinnedHeaderListViewAdapter;
import lb.library.StringArrayAlphabetIndexer;
import lb.listviewvariants.utils.CircularContactView;
import lb.listviewvariants.utils.ContactImageUtil;
import lb.listviewvariants.utils.ContactsQuery;
import lb.listviewvariants.utils.ImageCache;
import lb.listviewvariants.utils.async_task_thread_pool.AsyncTaskEx;
import lb.listviewvariants.utils.async_task_thread_pool.AsyncTaskThreadPool;


public class FriendLVActivity extends ActionBarActivity {
    private LayoutInflater mInflater;
    private PinnedHeaderListView mListView;
    private ContactsAdapter mAdapter;

    private VKRequest myRequest;
    CharSequence[] vkApiUsersNames;

    FloatingActionButton fab;
    ArrayList<Contact> profileFriends;
    ContentLoadingProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInflater = LayoutInflater.from(FriendLVActivity.this);
        setContentView(R.layout.activity_main);

        profileFriends = new ArrayList<Contact>();
        if (savedInstanceState == null) {
            processRequestIfRequired();
        }
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListView = (PinnedHeaderListView) findViewById(android.R.id.list);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(mListView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FriendLVActivity.this, InfoGraphicFriendActivity.class);
                i.putExtra("request", myRequest.registerObject());
                startActivity(i);
            }
        });
        progressBar = (ContentLoadingProgressBar) findViewById(R.id.pb_friend);
        progressBar.show();

        mAdapter = new ContactsAdapter(new ArrayList<Contact>());
        mAdapter.setPinnedHeaderBackgroundColor(getResources().getColor(getResIdFromAttribute(this, android.R.attr.colorBackground)));
        mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.pinned_header_text));
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);

        // int pinnedHeaderBackgroundColor = getResources().getColor(getResIdFromAttribute(this, android.R.attr.colorBackground));
        mListView.setPinnedHeaderView(mInflater.inflate(R.layout.pinned_header_listview_side_header, mListView, false));
        mListView.setEnableHeaderTransparencyChanges(false);
        //    mAdapter.getFilter().filter(mQueryText,new FilterListener() ...
        //You can also perform operations on selected item by using :
        //    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() ...
    }

    public void setAdapter() {
        progressBar.hide();
        //s  final ArrayList<Contact> contacts = getContacts();
        final ArrayList<Contact> contacts = profileFriends;
        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                char lhsFirstLetter = TextUtils.isEmpty(lhs.displayName) ? ' ' : lhs.displayName.charAt(0);
                char rhsFirstLetter = TextUtils.isEmpty(rhs.displayName) ? ' ' : rhs.displayName.charAt(0);
                int firstLetterComparison = Character.toUpperCase(lhsFirstLetter) - Character.toUpperCase(rhsFirstLetter);
                if (firstLetterComparison == 0)
                    return lhs.displayName.compareTo(rhs.displayName);
                return firstLetterComparison;
            }
        });
        mAdapter = new ContactsAdapter(contacts);
        mAdapter.setPinnedHeaderBackgroundColor(getResources().getColor(getResIdFromAttribute(this, android.R.attr.colorBackground)));
        mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.pinned_header_text));
        mListView.setAdapter(mAdapter);
       /* mListView.setOnScrollListener(mAdapter);
        fab.attachToListView(mListView);*/
    }

    public static int getResIdFromAttribute(final Activity activity, final int attr) {
        if (attr == 0)
            return 0;
        final TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.resourceId;
    }

    private ArrayList<Contact> getContacts() {
        if (checkContactsReadPermission()) {
            Uri uri = ContactsQuery.CONTENT_URI;
            final Cursor cursor = managedQuery(uri, ContactsQuery.PROJECTION, ContactsQuery.SELECTION, null, ContactsQuery.SORT_ORDER);
            if (cursor == null)
                return null;
            ArrayList<Contact> result = new ArrayList<>();
            while (cursor.moveToNext()) {
                Contact contact = new Contact();
                contact.contactUri = ContactsContract.Contacts.getLookupUri(
                        cursor.getLong(ContactsQuery.ID),
                        cursor.getString(ContactsQuery.LOOKUP_KEY));
                contact.image = ContactsContract.Contacts.getLookupUri(
                        cursor.getLong(ContactsQuery.ID),
                        cursor.getString(ContactsQuery.LOOKUP_KEY)).toString();
                contact.displayName = cursor.getString(ContactsQuery.DISPLAY_NAME);
                contact.photoId = cursor.getString(ContactsQuery.PHOTO_THUMBNAIL_DATA);
                result.add(contact);
            }

            return result;
        }
        ArrayList<Contact> result = new ArrayList<>();
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; ++i) {
            Contact contact = new Contact();
            sb.delete(0, sb.length());
            int strLength = r.nextInt(10) + 1;
            for (int j = 0; j < strLength; ++j)
                switch (r.nextInt(3)) {
                    case 0:
                        sb.append((char) ('a' + r.nextInt('z' - 'a')));
                        break;
                    case 1:
                        sb.append((char) ('A' + r.nextInt('Z' - 'A')));
                        break;
                    case 2:
                        sb.append((char) ('0' + r.nextInt('9' - '0')));
                        break;
                }

            contact.displayName = sb.toString();
            result.add(contact);
        }
        return result;
    }

    private boolean checkContactsReadPermission() {
        String permission = "android.permission.READ_CONTACTS";
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.mAsyncTaskThreadPool.cancelAllTasks(true);
    }

    private static class Contact extends Profile {
        long contactId;
        Uri contactUri;
        String displayName;
        String photoId;
        boolean online;
        boolean monline;

        public Contact() {
            super(null, null, false);
        }

        public Contact(String _describe, String _image, boolean _online, boolean _monline) {
            super(_describe, _image, _online);
            displayName = _describe;
            online = _online;
            monline = _monline;
            if (_monline) {
                online = _monline;
            }
            //  photoId = _image;
            //contactUri = new Uri
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

    // ////////////////////////////////////////////////////////////
    // ContactsAdapter //
    // //////////////////
    private class ContactsAdapter extends SearchablePinnedHeaderListViewAdapter<Contact> {
        private ArrayList<Contact> mContacts;
        private final int CONTACT_PHOTO_IMAGE_SIZE;
        private final int[] PHOTO_TEXT_BACKGROUND_COLORS;
        private final AsyncTaskThreadPool mAsyncTaskThreadPool = new AsyncTaskThreadPool(1, 2, 10);

        @Override
        public CharSequence getSectionTitle(int sectionIndex) {
            return ((StringArrayAlphabetIndexer.AlphaBetSection) getSections()[sectionIndex]).getName();
        }

        public ContactsAdapter(final ArrayList<Contact> contacts) {
            setData(contacts);
            PHOTO_TEXT_BACKGROUND_COLORS = getResources().getIntArray(R.array.contacts_text_background_colors);
            CONTACT_PHOTO_IMAGE_SIZE = getResources().getDimensionPixelSize(
                    R.dimen.list_item__contact_imageview_size);
        }

        public void setData(final ArrayList<Contact> contacts) {
            this.mContacts = contacts;
            final String[] generatedContactNames = generateContactNames(contacts);
            setSectionIndexer(new StringArrayAlphabetIndexer(generatedContactNames, true));
        }

        private String[] generateContactNames(final List<Contact> contacts) {
            final ArrayList<String> contactNames = new ArrayList<String>();
            if (contacts != null)
                for (final Contact contactEntity : contacts)
                    contactNames.add(contactEntity.displayName);
            return contactNames.toArray(new String[contactNames.size()]);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final ViewHolder holder;
            final View rootView;
            if (convertView == null) {
                holder = new ViewHolder();
                rootView = mInflater.inflate(R.layout.listview_item, parent, false);
                holder.friendProfileCircularContactView = (CircularContactView) rootView
                        .findViewById(R.id.listview_item__friendPhotoImageView);
                holder.onlineImage = (ImageView) rootView
                        .findViewById(R.id.online);
                holder.friendProfileCircularContactView.getTextView().setTextColor(0xFFffffff);
                holder.friendName = (TextView) rootView
                        .findViewById(R.id.listview_item__friendNameTextView);
                holder.headerView = (TextView) rootView.findViewById(R.id.header_text);
                rootView.setTag(holder);
            } else {
                rootView = convertView;
                holder = (ViewHolder) rootView.getTag();
            }
            final Contact contact = getItem(position);
            final String displayName = contact.displayName;
            holder.friendName.setText(displayName);

            if (contact.online) {
                if (contact.monline) {
                    holder.onlineImage.setImageResource(R.drawable.phone);
                } else {
                    holder.onlineImage.setImageResource(R.drawable.co);
                }
            } else {
                holder.onlineImage.setImageResource(0);
            }

            boolean hasPhoto = !TextUtils.isEmpty(contact.photoId);
            if (holder.updateTask != null && !holder.updateTask.isCancelled())
                holder.updateTask.cancel(true);
            final Bitmap cachedBitmap = hasPhoto ? ImageCache.INSTANCE.getBitmapFromMemCache(contact.photoId) : null;
            if (!contact.image.contains("deact")) {
                holder.friendProfileCircularContactView.loadBitmap(FriendLVActivity.this, contact.image);
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
                            final Bitmap b = ContactImageUtil.loadContactPhotoThumbnail(FriendLVActivity.this, contact.photoId, CONTACT_PHOTO_IMAGE_SIZE);
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
                            ImageCache.INSTANCE.addBitmapToCache(contact.photoId, result);
                            holder.friendProfileCircularContactView.setImageBitmap(result);
                        }
                    };
                    mAsyncTaskThreadPool.executeAsyncTask(holder.updateTask);
                }
            }

            bindSectionHeader(holder.headerView, null, position);
            return rootView;
        }

        @Override
        public boolean doFilter(final Contact item, final CharSequence constraint) {
            if (TextUtils.isEmpty(constraint))
                return true;
            final String displayName = item.displayName;
            return !TextUtils.isEmpty(displayName) && displayName.toLowerCase(Locale.getDefault())
                    .contains(constraint.toString().toLowerCase(Locale.getDefault()));
        }

        @Override
        public ArrayList<Contact> getOriginalList() {
            return mContacts;
        }


    }

    // /////////////////////////////////////////////////////////////////////////////////////
    // ViewHolder //
    // /////////////
    public static class ViewHolder {
        public CircularContactView friendProfileCircularContactView;
        public ImageView onlineImage;
        public TextView friendName, headerView;
        public AsyncTaskEx<Void, Void, Bitmap> updateTask;
    }

    private void processRequestIfRequired() {
        VKRequest request = null;

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().hasExtra("request")) {
            long requestId = getIntent().getExtras().getLong("request");
            request = VKRequest.getRegisteredRequest(requestId);
            if (request != null)
                request.unregisterObject();
        }

        if (request == null) return;
        myRequest = request;
        request.executeWithListener(mRequestListener);
    }

    public VKRequest.VKRequestListener mRequestListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            //setResponseText(response.json.toString());
            progressBar.show();
            JSONArray jsonArray = null;
            try {
                jsonArray = response.json.getJSONObject("response").getJSONArray("items");

                int length = jsonArray.length();
                // vkApiUsers = new VKApiUser[length];
                vkApiUsersNames = new CharSequence[length];

                for (int i = 0; i < length; i++) {
                    VKApiUser user = new VKApiUser(jsonArray.getJSONObject(i));
                    vkApiUsersNames[i] = user.first_name + " " + user.last_name;
                    // + " " + ((user.sex == 1) ? "Female" : "Male");
                    profileFriends.add(new Contact(vkApiUsersNames[i].toString(), user.photo_50, user.online, user.online_mobile));
                }
                setAdapter();
                //  setResponseText(sb.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(VKError error) {
            //setResponseText(error.toString());
            //   progressBar.hide();
        }

        @Override
        public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded,
                               long bytesTotal) {
            //  progressBar.show();  // you can show progress of the request if you want
        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
            // progressBar.hide();
        }
    };
}