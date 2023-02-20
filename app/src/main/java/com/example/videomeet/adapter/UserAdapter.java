package com.example.videomeet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.videomeet.R;
import com.example.videomeet.listeners.UserListeners;
import com.example.videomeet.models.User;
import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private ArrayList<User> users;
    public UserListeners userListeners;

    public UserAdapter(ArrayList<User> users, UserListeners userListeners){
        this.users = users;
        this.userListeners = userListeners;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.user_list_template,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        TextView textFirstChar, textUsername, textEmail;
        ImageView imageAudioMeeting, imageVideoMeeting;


        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textFirstChar = itemView.findViewById(R.id.TextFirstChar);
            textUsername = itemView.findViewById(R.id.userNameTemp);
            textEmail = itemView.findViewById(R.id.userEmailTemp);

            imageAudioMeeting = itemView.findViewById(R.id.imageCall);
            imageVideoMeeting = itemView.findViewById(R.id.imageVideo);
        }

        void setUserData(User user) {
            textFirstChar.setText(user.Name.substring(0,1));
            textUsername.setText(user.Name);
            textEmail.setText(user.Email);
            imageAudioMeeting.setOnClickListener(view -> userListeners.initiateAudioMeeting(user));
            imageVideoMeeting.setOnClickListener(view -> userListeners.initiateVideoMeeting(user));
        }

    }

}
