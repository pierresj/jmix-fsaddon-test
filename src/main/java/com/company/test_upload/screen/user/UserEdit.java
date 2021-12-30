package com.company.test_upload.screen.user;

import com.company.test_upload.entity.User;
import io.jmix.core.EntityStates;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.*;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import io.jmix.ui.upload.TemporaryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.TimeZone;

@UiController("User.edit")
@UiDescriptor("user-edit.xml")
@EditedEntityContainer("userDc")
@Route(value = "users/edit", parentPrefix = "users")
public class UserEdit extends StandardEditor<User> {

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordField passwordField;

    @Autowired
    private TextField<String> usernameField;

    @Autowired
    private PasswordField confirmPasswordField;

    @Autowired
    private Notifications notifications;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private ComboBox<String> timeZoneField;

    @Autowired
    private FileStorageUploadField manuallyControlledField;

    @Autowired
    private TemporaryStorage temporaryStorage;

    @Autowired
    private FileStorage fileStorage;

    @Subscribe
    public void onInitEntity(InitEntityEvent<User> event) {
        usernameField.setEditable(true);
        passwordField.setVisible(true);
        confirmPasswordField.setVisible(true);
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            usernameField.focus();
        }
    }

    @Subscribe
    protected void onBeforeCommit(BeforeCommitChangesEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            if (!Objects.equals(passwordField.getValue(), confirmPasswordField.getValue())) {
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption(messageBundle.getMessage("passwordsDoNotMatch"))
                        .show();
                event.preventCommit();
            }
            getEditedEntity().setPassword(passwordEncoder.encode(passwordField.getValue()));
        }
    }

    @Subscribe
    public void onInit(InitEvent event) {
        timeZoneField.setOptionsList(Arrays.asList(TimeZone.getAvailableIDs()));
    }

    @Subscribe("manuallyControlledField")
    public void onManuallyControlledFieldFileUploadSucceed(SingleFileUploadField.FileUploadSucceedEvent event) throws FileNotFoundException {
        File file = temporaryStorage.getFile(manuallyControlledField.getFileId());
        if (file != null) {
            notifications.create()
                    .withCaption("File is uploaded to temporary storage at " + file.getAbsolutePath())
                    .show();
        }
        InputStream fileInputStream = new FileInputStream(file);

        //FileRef fileRef = temporaryStorage.putFileIntoStorage(manuallyControlledField.getFileId(), event.getFileName());
        FileRef fileRef = fileStorage.saveStream(event.getFileName(), fileInputStream);

        manuallyControlledField.setValue(fileRef);
        notifications.create()
                .withCaption("Uploaded file: " + manuallyControlledField.getFileName())
                .show();
    }
    @Subscribe("manuallyControlledField")
    public void onManuallyControlledFieldFileUploadError(SingleFileUploadField.FileUploadErrorEvent event) {
        notifications.create()
                .withCaption("File upload error")
                .show();
    }
}