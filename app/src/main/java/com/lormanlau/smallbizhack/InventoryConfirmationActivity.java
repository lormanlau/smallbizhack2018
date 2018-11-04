package com.lormanlau.smallbizhack;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

public class InventoryConfirmationActivity extends AppCompatActivity {

    EditText mAmountEditText, mItemTagsEditText;
    Button confirmButton;
    ImageView mImageView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_confirmation);

        mAmountEditText = findViewById(R.id.itemAmount);
        mItemTagsEditText = findViewById(R.id.itemTag);

        confirmButton = findViewById(R.id.confirmButton);

        mImageView = findViewById(R.id.itemImageView);

        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("filePath");
        File imageFile = new File(imagePath);
        mImageView.setImageURI(Uri.fromFile(imageFile));


        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("itemName", mItemTagsEditText.getText().toString());
                resultIntent.putExtra("itemAmount", mAmountEditText.getText().toString());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
