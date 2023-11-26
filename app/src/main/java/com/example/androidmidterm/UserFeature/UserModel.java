package com.example.androidmidterm.UserFeature;

import com.example.androidmidterm.UserFeature.USER_ROLE;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;

public class UserModel {
    private String userID;
    USER_ROLE userRole;
    private String userName;
    private int userAge;
    private String userPhone;
    private boolean userStatus;
    private String userImage;
    private ArrayList<LoginHistory> createLoginHistory;
    public static class LoginHistory {
        @ServerTimestamp
        private Date time;

        // Constructors, getters, setters

        public LoginHistory() {
            // Default constructor required for Firebase
        }

        public LoginHistory(Date time) {
            this.time = time;
        }
        public Date getTime() {
            return time;
        }
        public void setTime(Date time) {
            this.time = time;
        }
    }

    public UserModel() {
    }

    public UserModel(String userID, USER_ROLE userRole, String userName, int userAge, String userPhone, boolean userStatus, String userImage, ArrayList<LoginHistory> loginHistory) {
        this.userID = userID;
        this.userRole = userRole;
        this.userName = userName;
        this.userAge = userAge;
        this.userPhone = userPhone;
        this.userStatus = userStatus;
        this.userImage = userImage;
        this.createLoginHistory = loginHistory;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public USER_ROLE getUserRole() {
        return userRole;
    }

    public void setUserRole(USER_ROLE userRole) {
        this.userRole = userRole;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserAge() {
        return userAge;
    }

    public void setUserAge(int userAge) {
        this.userAge = userAge;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public boolean isUserStatus() {
        return userStatus;
    }

    public void setUserStatus(boolean userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public ArrayList<LoginHistory> getLoginHistory() {
        return createLoginHistory;
    }

    public void setLoginHistory(ArrayList<LoginHistory> loginHistory) {
        this.createLoginHistory = loginHistory;
    }

    public void setUserRoleWithString(String userRole) {
        switch (userRole) {
            case "ADMIN":
                this.userRole = USER_ROLE.ADMIN;
                break;
            case "MANAGER":
                this.userRole = USER_ROLE.MANAGER;
                break;
            case "EMPLOYEE":
                this.userRole = USER_ROLE.EMPLOYEE;
                break;
        }
    }
}
