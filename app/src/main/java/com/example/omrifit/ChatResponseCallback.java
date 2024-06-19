package com.example.omrifit;


/**
 * return the chat or an error depends if the callBack was successful
 */
public interface ChatResponseCallback {
    /**
     * if the callBack was successful
     * @param chat
     */
     void onSuccess(Chat chat);

    /**
     * if was an error
     * @param error
     */
     void onError(String error);
}
