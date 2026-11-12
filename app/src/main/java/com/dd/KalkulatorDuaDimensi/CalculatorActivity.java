package com.dd.KalkulatorDuaDimensi;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import java.math.BigDecimal;

public class CalculatorActivity extends AppCompatActivity {
    static final String EXTRA_SHAPE = "com.dd.KalkulatorDuaDimensi.SHAPE";
    private Shape shape;
    private EditText[] inputs;
    private TextView areaResult;
    private TextView perimeterResult;
    private boolean hasResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shape = Shape.fromKey(getIntent().getStringExtra(EXTRA_SHAPE));
        if (shape == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_calculator);
        String title = getString(R.string.calculator_title, getString(shape.title));
        setTitle(title);
        ((TextView) findViewById(R.id.tvTitle)).setText(title);
        ((TextView) findViewById(R.id.tvDescription)).setText(shape.description);
        ViewCompat.setAccessibilityHeading(findViewById(R.id.tvTitle), true);
        ViewCompat.setAccessibilityHeading(findViewById(R.id.tvResultTitle), true);
        areaResult = findViewById(R.id.tvResult);
        perimeterResult = findViewById(R.id.tvPerimeter);

        TextWatcher clearOnEdit = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearResult();
                for (EditText input : inputs) {
                    input.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        int[] rows = {R.id.inputRow1, R.id.inputRow2, R.id.inputRow3, R.id.inputRow4};
        int[] labels = {R.id.inputLabel1, R.id.inputLabel2, R.id.inputLabel3, R.id.inputLabel4};
        int[] fields = {R.id.inputDimension1, R.id.inputDimension2,
                R.id.inputDimension3, R.id.inputDimension4};
        inputs = new EditText[shape.labels.length];
        for (int i = 0; i < rows.length; i++) {
            View row = findViewById(rows[i]);
            if (i >= inputs.length) {
                row.setVisibility(View.GONE);
                continue;
            }
            ((TextView) findViewById(labels[i])).setText(shape.labels[i]);
            inputs[i] = findViewById(fields[i]);
            inputs[i].addTextChangedListener(clearOnEdit);
        }

        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnCalculate).setOnClickListener(view -> calculate());
        findViewById(R.id.btnReset).setOnClickListener(view -> {
            for (EditText input : inputs) {
                input.setText("");
                input.setError(null);
            }
            inputs[0].requestFocus();
        });
        EditText lastInput = inputs[inputs.length - 1];
        lastInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        lastInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                calculate();
                return true;
            }
            return false;
        });
    }

    private void calculate() {
        BigDecimal[] dimensions = new BigDecimal[inputs.length];
        EditText firstInvalid = null;
        for (int i = 0; i < inputs.length; i++) {
            dimensions[i] = readDimension(inputs[i]);
            if (dimensions[i] == null && firstInvalid == null) {
                firstInvalid = inputs[i];
            }
        }
        if (firstInvalid != null) {
            clearResult();
            firstInvalid.requestFocus();
            return;
        }

        ShapeCalculator.Measurements measurements = ShapeCalculator.calculate(shape, dimensions);
        if (measurements == null) {
            clearResult();
            EditText lastInput = inputs[inputs.length - 1];
            lastInput.setError(getString(shape.invalidShape));
            lastInput.requestFocus();
            return;
        }

        areaResult.setText(formatResult(shape.areaResult, measurements.area, measurements.areaInputs));
        perimeterResult.setText(formatResult(shape.perimeterResult, measurements.perimeter, dimensions));
        hasResult = true;
    }

    private String formatResult(int resource, BigDecimal result, BigDecimal[] operands) {
        Object[] values = new Object[operands.length + 1];
        values[0] = format(result);
        for (int i = 0; i < operands.length; i++) {
            values[i + 1] = format(operands[i]);
        }
        return getString(resource, values);
    }

    private void clearResult() {
        hasResult = false;
        areaResult.setText(R.string.area_empty);
        perimeterResult.setText(R.string.perimeter_empty);
    }

    private BigDecimal readDimension(EditText input) {
        input.setError(null);
        String text = input.getText().toString().trim();
        if (text.isEmpty()) {
            input.setError(getString(R.string.required_input));
            return null;
        }
        if (text.matches("(?:[0-9]+(?:[.,][0-9]*)?|[.,][0-9]+)")) {
            BigDecimal value = new BigDecimal(text.replace(',', '.'));
            if (value.signum() > 0) {
                return value;
            }
        }
        input.setError(getString(R.string.invalid_input));
        return null;
    }

    private String format(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("hasResult", hasResult);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean("hasResult")) {
            calculate();
        }
    }
}
