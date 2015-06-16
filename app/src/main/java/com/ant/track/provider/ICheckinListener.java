package com.ant.track.provider;

import com.ant.track.models.User;

/**
 * Created by Toader on 6/4/2015.
 */
public interface ICheckinListener {
    /**
     * updates the checkin for a specific user.
     *
     * @param user the user for which we want to update checkin.
     */
    void updateCheckin(User user);
}
