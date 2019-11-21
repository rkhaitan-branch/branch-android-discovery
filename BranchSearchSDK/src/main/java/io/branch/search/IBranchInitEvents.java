package io.branch.search;

/**
 * Events called  when Branch Search SDK is initialized.
 */
public interface IBranchInitEvents {
    /**
     * Called when there is no Location permission granted to the app. Location permissions are optional for Branch Search SDK but provides better search results if granted.
     */
    void onOptionalLocationPermissionMissing();
}
