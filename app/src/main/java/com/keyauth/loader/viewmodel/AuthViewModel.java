package com.keyauth.loader.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AuthViewModel extends ViewModel {
    
    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> subscription = new MutableLiveData<>();
    
    public AuthViewModel() {
        // Initialize with default values - will be updated when user authenticates
        username.setValue("Authenticated User");
        subscription.setValue("Premium");
    }
    
    public LiveData<String> getUsername() {
        return username;
    }
    
    public LiveData<String> getSubscription() {
        return subscription;
    }
    
    public void setUsername(String username) {
        this.username.setValue(username);
    }
    
    public void setSubscription(String subscription) {
        this.subscription.setValue(subscription);
    }
}
