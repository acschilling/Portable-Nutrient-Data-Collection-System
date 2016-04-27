package com.ndca.nutrientdatacollectionapp;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Created by polar_cyclone12 on 4/23/2016.
 */
public class SampleActivity extends AppCompatActivity {

    LineGraphSeries<DataPoint> series;
    InputStream stream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        Viewport viewport = graph.getViewport();
        viewport.setScrollable(true);
        graph.setTitle("Wavelength(nm) V Intensity");

        AssetManager assetManager = getAssets();
        try {
            stream = assetManager.open("data_text.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scanner getMinX = new Scanner(new InputStreamReader(stream));
        getMinX.next();
        viewport.setMinX(Double.parseDouble(getMinX.next()));
        Scanner scan = new Scanner(new InputStreamReader(stream));
        scan.next();
            while(scan.hasNext()){
                String cur = scan.next();

                addToGraph(new DataPoint(Double.parseDouble(cur), Double.parseDouble(scan.next())));

            }

        graph.addSeries(series);
        getMinX.close();
        scan.close();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addToGraph(DataPoint dataPoint){

        series.appendData(dataPoint,true,37000);

    }

}
