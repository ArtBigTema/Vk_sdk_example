package group.finder;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.perm.kate.api.Account;
import com.perm.kate.api.Api;
import com.perm.kate.api.Constants;
import com.perm.kate.api.Group;
import com.vk.Sub.Contact;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.vktestapp.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class FindMe extends ActionBarActivity {
    private VKRequest myRequest;
    List<Group> vkApiGroups;

    EditText etGroup;
    EditText etMe;
    ActionProcessButton buttonFindMe;
    ActionProcessButton buttonFindGroup;
    ListView groupsLV;
    Account account = new Account();
    Api api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_me);

        etMe = (EditText) findViewById(R.id.et_me);
        etGroup = (EditText) findViewById(R.id.et_group);
        buttonFindGroup = (ActionProcessButton) findViewById(R.id.btn_find_group);
        buttonFindMe = (ActionProcessButton) findViewById(R.id.btn_find_me);
        groupsLV = (ListView) findViewById(R.id.lv_search_group);

        account.restore(this);

        etGroup.setText(account.access_token + "|" + account.user_id);
        api = new Api(account.access_token, Constants.API_ID);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        buttonFindGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKRequest request = VKApi.groups().get(VKParameters.from(VKApiConst.EXTENDED, 1));
                request.executeWithListener(mRequestListener);
            }
        });
        groupsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etMe.setVisibility(View.VISIBLE);
                etMe.setText(parent.getItemAtPosition(position).toString());
                groupsLV.setVisibility(View.GONE);

                buttonFindMe.setVisibility(View.VISIBLE);
                buttonFindMe.setMode(ActionProcessButton.Mode.ENDLESS);
                buttonFindGroup.setMode(ActionProcessButton.Mode.ENDLESS);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        //  super.onBackPressed();
        if (buttonFindGroup.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
        }
        if (groupsLV.getVisibility() == View.VISIBLE) {
            groupsLV.setVisibility(View.GONE);
            buttonFindGroup.setVisibility(View.VISIBLE);
            buttonFindGroup.setProgress(0);
            etGroup.setVisibility(View.VISIBLE);
        }
        if (buttonFindMe.getVisibility() == View.VISIBLE) {
            buttonFindMe.setVisibility(View.GONE);
            etMe.setVisibility(View.GONE);
            groupsLV.setVisibility(View.VISIBLE);
        }
    }

    public VKRequest.VKRequestListener mRequestListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            buttonFindGroup.setProgress(25);
            JSONArray jsonArray = null;
            try {
                jsonArray = response.json.getJSONObject("response").getJSONArray("items");
                buttonFindGroup.setProgress(50);
                vkApiGroups = Group.parseGroups(jsonArray);
                buttonFindGroup.setProgress(100);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                //  buttonFindGroup.setMode(ActionProcessButton.Mode.ENDLESS);
                etGroup.setVisibility(View.GONE);
                buttonFindGroup.setVisibility(View.GONE);
                groupsLV.setVisibility(View.VISIBLE);
                GroupAdapter g = new GroupAdapter(FindMe.this, R.layout.listview_item, getContactFromGroup());
                groupsLV.setAdapter(g);
            }
        }

        @Override
        public void onError(VKError error) {
            buttonFindGroup.setMode(ActionProcessButton.Mode.ENDLESS);
        }

        @Override
        public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded,
                               long bytesTotal) {
            buttonFindGroup.setMode(ActionProcessButton.Mode.PROGRESS);
            buttonFindGroup.setProgress(1);
        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
            buttonFindGroup.setMode(ActionProcessButton.Mode.ENDLESS);
        }
    };

    public List<Contact> getContactFromGroup() {
        List<Contact> contacts = new ArrayList<Contact>();
        for (Group g : vkApiGroups) {
            contacts.add(new Contact(g.name, g.photo, false, false));
        }
        return contacts;
    }
}