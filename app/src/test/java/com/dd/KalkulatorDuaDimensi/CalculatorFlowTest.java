package com.dd.KalkulatorDuaDimensi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.TextView;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35, qualifiers = "w320dp-h480dp")
public class CalculatorFlowTest {
    private static final int[] INPUT_IDS = {R.id.inputDimension1, R.id.inputDimension2,
            R.id.inputDimension3, R.id.inputDimension4};
    private static final int[] ROW_IDS = {R.id.inputRow1, R.id.inputRow2, R.id.inputRow3, R.id.inputRow4};
    private static final String[][] EXAMPLES = {
            {"5.5", "30,25", "22"},
            {"12,5", "4.2", "52,5", "33,4"},
            {"3", "4", "5", "6", "12"},
            {"4", "10", "5", "5", "28", "24"},
            {"5", "9", "24", "168", "56"},
            {"10", "5", "4", "40", "30"},
            {"6", "8", "24", "20"},
            {"7", "153,938040026", "43,9822971503"},
            {"4", "27,5276384094", "20"},
            {"4", "41,5692193817", "24"}
    };

    @Test
    public void allMenuChoicesOpenTheRightCalculatorAndShowBothResults() {
        try (ActivityController<MainActivity> home =
                     Robolectric.buildActivity(MainActivity.class).setup().visible()) {
            GridView menu = home.get().findViewById(R.id.shapeMenu);
            Shape[] shapes = Shape.values();
            assertEquals(10, menu.getCount());
            for (int position = 0; position < shapes.length; position++) {
                Shape shape = shapes[position];
                Button choice = (Button) menu.getAdapter().getView(position, null, menu);
                assertEquals(home.get().getString(shape.title), choice.getText().toString());
                assertTrue(choice.performClick());
                Intent intent = shadowOf(home.get()).getNextStartedActivity();
                assertEquals(CalculatorActivity.class.getName(), intent.getComponent().getClassName());
                assertEquals(shape.name(), intent.getStringExtra(CalculatorActivity.EXTRA_SHAPE));
                assertEquals(intent.getComponent(),
                        intent.resolveActivity(home.get().getPackageManager()));

                try (ActivityController<CalculatorActivity> controller =
                             Robolectric.buildActivity(CalculatorActivity.class, intent).setup()) {
                    CalculatorActivity calculator = controller.get();
                    for (int i = 0; i < ROW_IDS.length; i++) {
                        assertEquals(i < shape.labels.length ? View.VISIBLE : View.GONE,
                                calculator.findViewById(ROW_IDS[i]).getVisibility());
                    }
                    String[] example = EXAMPLES[position];
                    fill(calculator, example, shape.labels.length);
                    calculator.findViewById(R.id.btnCalculate).performClick();
                    assertTrue(area(calculator).endsWith(example[shape.labels.length] + " cm²"));
                    assertTrue(perimeter(calculator).endsWith(example[shape.labels.length + 1] + " cm"));
                    assertFalse(area(calculator).contains("%"));
                    assertFalse(perimeter(calculator).contains("%"));
                    calculator.findViewById(R.id.btnBack).performClick();
                    assertTrue(calculator.isFinishing());
                }
            }
        }
    }

    @Test
    public void invalidNumbersAreRejectedAndZeroResultsAreNotDisplayed() {
        try (ActivityController<CalculatorActivity> controller = calculator(Shape.PERSEGI)) {
            CalculatorActivity activity = controller.get();
            EditText side = activity.findViewById(R.id.inputDimension1);
            for (String invalid : new String[] {"", "0", "0,0", "1..2", "1,2.3"}) {
                side.setText(invalid);
                activity.findViewById(R.id.btnCalculate).performClick();
                assertNotNull("Input " + invalid, side.getError());
                assertEquals(activity.getString(R.string.area_empty), area(activity));
            }
            side.setText("0.00001");
            activity.findViewById(R.id.btnCalculate).performClick();
            assertTrue(area(activity).endsWith("0,0000000001 cm²"));
        }
    }

    @Test
    public void invalidGeometryShowsAnErrorAndEditingAllowsRecovery() {
        assertInvalidGeometry(Shape.SEGITIGA, new String[] {"1", "2", "3"});
        assertInvalidGeometry(Shape.TRAPESIUM, new String[] {"4", "10", "2", "2"});
        assertInvalidGeometry(Shape.TRAPESIUM, new String[] {"8", "8", "5", "5"});
        assertInvalidGeometry(Shape.JAJAR_GENJANG, new String[] {"10", "3", "4"});

        try (ActivityController<CalculatorActivity> controller = calculator(Shape.SEGITIGA)) {
            CalculatorActivity activity = controller.get();
            fill(activity, new String[] {"1", "2", "3"}, 3);
            activity.findViewById(R.id.btnCalculate).performClick();
            EditText last = activity.findViewById(R.id.inputDimension3);
            assertNotNull(last.getError());
            fill(activity, new String[] {"3", "4", "5"}, 3);
            assertNull(last.getError());
            activity.findViewById(R.id.btnCalculate).performClick();
            assertTrue(area(activity).endsWith("6 cm²"));
        }
    }

    @Test
    public void keyboardRecreationEditingAndResetPreserveTheExpectedState() {
        try (ActivityController<CalculatorActivity> controller = calculator(Shape.PERSEGI_PANJANG)) {
            CalculatorActivity activity = controller.get();
            fill(activity, new String[] {"12,5", "4.2"}, 2);
            ((EditText) activity.findViewById(R.id.inputDimension2))
                    .onEditorAction(EditorInfo.IME_ACTION_DONE);
            assertTrue(area(activity).endsWith("52,5 cm²"));
            activity = controller.recreate().get();
            assertTrue(area(activity).endsWith("52,5 cm²"));
            assertTrue(perimeter(activity).endsWith("33,4 cm"));
            ((EditText) activity.findViewById(R.id.inputDimension1)).setText("10");
            assertEquals(activity.getString(R.string.area_empty), area(activity));
            activity.findViewById(R.id.btnCalculate).performClick();
            activity.findViewById(R.id.btnReset).performClick();
            for (int i = 0; i < 2; i++) {
                EditText input = activity.findViewById(INPUT_IDS[i]);
                assertEquals("", input.getText().toString());
                assertNull(input.getError());
            }
            assertEquals(activity.getString(R.string.area_empty), area(activity));
        }
    }

    @Test
    public void missingAndUnknownChoicesCloseTheCalculatorSafely() {
        for (String choice : new String[] {null, "NOT_A_SHAPE"}) {
            Intent intent = new Intent();
            intent.putExtra(CalculatorActivity.EXTRA_SHAPE, choice);
            try (ActivityController<CalculatorActivity> controller =
                         Robolectric.buildActivity(CalculatorActivity.class, intent).create()) {
                assertTrue(controller.get().isFinishing());
            }
        }
    }

    @Test
    public void menuUsesOneColumnOnANarrowScreen() {
        try (ActivityController<MainActivity> controller =
                     Robolectric.buildActivity(MainActivity.class).setup().visible()) {
            GridView menu = controller.get().findViewById(R.id.shapeMenu);
            assertEquals(1, menu.getNumColumns());
            assertTrue(menu.getChildAt(0).getHeight() >= 48);
        }
    }

    @Test
    @Config(qualifiers = "w840dp-h900dp")
    public void menuUsesOneColumnOnAWideScreen() {
        try (ActivityController<MainActivity> controller =
                     Robolectric.buildActivity(MainActivity.class).setup().visible()) {
            GridView menu = controller.get().findViewById(R.id.shapeMenu);
            assertEquals(1, menu.getNumColumns());
        }
    }

    @Test
    @Config(qualifiers = "w480dp-h320dp-land")
    public void longCalculatorRemainsScrollableOnAShortScreen() {
        try (ActivityController<CalculatorActivity> controller =
                     calculator(Shape.TRAPESIUM).visible()) {
            ViewGroup content = controller.get().findViewById(android.R.id.content);
            ScrollView scroll = (ScrollView) content.getChildAt(0);
            assertTrue(scroll.canScrollVertically(1));
            scroll.fullScroll(View.FOCUS_DOWN);
            assertTrue(scroll.getScrollY() > 0);
        }
    }

    @Test
    public void calculationsHandleVerySmallSidesAndRejectNonpositiveSizes() {
        assertNull(ShapeCalculator.calculate(Shape.PERSEGI,
                new BigDecimal[] {BigDecimal.ZERO}));
        ShapeCalculator.Measurements result = ShapeCalculator.calculate(Shape.SEGITIGA,
                new BigDecimal[] {new BigDecimal("99999999999999999999"),
                        new BigDecimal("99999999999999999999"), new BigDecimal("0.000000000000000001")});
        assertNotNull(result);
        assertEquals(0, new BigDecimal("50").compareTo(result.area));
    }

    private ActivityController<CalculatorActivity> calculator(Shape shape) {
        Intent intent = new Intent();
        intent.putExtra(CalculatorActivity.EXTRA_SHAPE, shape.name());
        return Robolectric.buildActivity(CalculatorActivity.class, intent).setup();
    }

    private void fill(CalculatorActivity activity, String[] values, int count) {
        for (int i = 0; i < count; i++) {
            ((EditText) activity.findViewById(INPUT_IDS[i])).setText(values[i]);
        }
    }

    private String area(CalculatorActivity activity) {
        return ((TextView) activity.findViewById(R.id.tvResult)).getText().toString();
    }

    private String perimeter(CalculatorActivity activity) {
        return ((TextView) activity.findViewById(R.id.tvPerimeter)).getText().toString();
    }

    private void assertInvalidGeometry(Shape shape, String[] values) {
        try (ActivityController<CalculatorActivity> controller = calculator(shape)) {
            CalculatorActivity activity = controller.get();
            fill(activity, values, values.length);
            activity.findViewById(R.id.btnCalculate).performClick();
            EditText last = activity.findViewById(INPUT_IDS[values.length - 1]);
            assertNotNull(last.getError());
            assertEquals(activity.getString(R.string.area_empty), area(activity));
        }
    }
}
