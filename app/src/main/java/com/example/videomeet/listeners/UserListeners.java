package com.example.videomeet.listeners;

import com.example.videomeet.models.User;

public interface UserListeners {

    void initiateVideoMeeting(User user);

    void initiateAudioMeeting(User user);

}
