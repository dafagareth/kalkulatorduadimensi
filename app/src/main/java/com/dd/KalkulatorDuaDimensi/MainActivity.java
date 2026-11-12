package com.dd.KalkulatorDuaDimensi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setAccessibilityHeading(findViewById(R.id.tvTitle), true);

        GridView menu = findViewById(R.id.shapeMenu);
        menu.setAdapter(new ArrayAdapter<Shape>(this, R.layout.item_shape, Shape.values()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Button button = (Button) convertView;
                if (button == null) {
                    button = (Button) getLayoutInflater().inflate(R.layout.item_shape, parent, false);
                }
                Shape shape = getItem(position);
                button.setText(shape.title);
                button.setOnClickListener(view -> {
                    Intent intent = new Intent(MainActivity.this, CalculatorActivity.class);
                    intent.putExtra(CalculatorActivity.EXTRA_SHAPE, shape.name());
                    startActivity(intent);
                });
                return button;
            }
        });
    }
}
