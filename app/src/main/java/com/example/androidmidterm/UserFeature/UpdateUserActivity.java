package com.example.androidmidterm.UserFeature;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.androidmidterm.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateUserActivity extends AppCompatActivity {
    private static final String TAG = "UpdateUserActivity";

    CircleImageView userImage;
    EditText etUserName, etUserAge, etUserPhone;
    RadioGroup rgRoles;
    Switch swStatus;
    Button btnUpdateUser;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        userImage = findViewById(R.id.userImage);
        etUserName = findViewById(R.id.etUserName);
        etUserAge = findViewById(R.id.etUserAge);
        etUserPhone = findViewById(R.id.etUserPhone);
        swStatus = findViewById(R.id.swStatus);
        rgRoles = findViewById(R.id.rgRoles);
        btnUpdateUser = findViewById(R.id.btnUpdateUser);
        imageUri = Uri.parse(getIntent().getStringExtra("userImage"));

        etUserName.setText(getIntent().getStringExtra("userName"));
        etUserAge.setText("" + getIntent().getIntExtra("userAge", 0));
        etUserPhone.setText(getIntent().getStringExtra("userPhone"));
        swStatus.setChecked(getIntent().getBooleanExtra("userStatus", false));
        switch (getIntent().getStringExtra("userRole")) {
            case "ADMIN":
                rgRoles.check(R.id.rbAdmin);
                break;
            case "MANAGER":
                rgRoles.check(R.id.rbManager);
                break;
            case "EMPLOYEE":
                rgRoles.check(R.id.rbEmployee);
                break;
        }
        Glide.with(this).load(getIntent().getStringExtra("userImage")).into(userImage);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        userImage.setImageURI(imageUri);
                    } else {
                        Toast.makeText(this, "No image is selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        userImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });

        btnUpdateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.putExtra("userName", etUserName.getText().toString());
                intent.putExtra("userAge", Integer.parseInt(etUserAge.getText().toString()));
                intent.putExtra("userPhone", etUserPhone.getText().toString());
                if (rgRoles.getCheckedRadioButtonId() == R.id.rbAdmin)
                    intent.putExtra("userRole", "ADMIN");
                else if (rgRoles.getCheckedRadioButtonId() == R.id.rbManager)
                    intent.putExtra("userRole", "MANAGER");
                else if (rgRoles.getCheckedRadioButtonId() == R.id.rbEmployee)
                    intent.putExtra("userRole", "EMPLOYEE");
                intent.putExtra("userStatus", swStatus.isChecked());
                intent.putExtra("userImage", imageUri.toString());
                setResult(1, intent);
                finish();
            }
        });
    }
}