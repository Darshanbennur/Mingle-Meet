package com.example.videomeet.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_FCM_TOKEN = "fcm_token";

    public static final String KEY_PREFERENCE_NAME = "videoMeetingPreferences";
    public static final String KEY_IS_SINGED_IN = "isSignedIn";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";

    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";

    public static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_MSG_INVITATION_CANCELED = "canceled";

    public static final String REMOTE_FRIEND_REQUEST_INVITATION = "friendRequest";
    public static final String REMOTE_FRIEND_REQUEST_ACCEPTED = "friendAccepted";
    public static final String REMOTE_FRIEND_REQUEST_REJECTED = "friendRejected";

    public static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";

    public static HashMap<String, String> getRemoteMessageHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(
                Constants.REMOTE_MSG_AUTHORIZATION,
                "key=AAAAgoCBDws:APA91bEj4NYnFDTddnd2kOnxmdfACL761iKowjcvOZ6kb3GQqpLYPV6MsVfVDcsRaNd0s9Ywkc8R0t_bPMot49eibpnhD_h0csVqUIC8JhXj6fGLhX8-IejiZsDgXi_z6706dbzuC7Xp"
        );
        headers.put(Constants.REMOTE_MSG_CONTENT_TYPE, "application/json");
        return headers;
    }

}
