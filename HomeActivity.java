package black.com.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;


import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;

import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import black.com.myapplication.Adapter.CustomViewPager;
import black.com.myapplication.BroadcastReceiver.LocationBroadcastReceiver;
import black.com.myapplication.Services.LocationBackgroundService;
import black.com.myapplication.Transformations.DepthTransformation;
import black.com.myapplication.Transformations.ZoomOutTransformation;
import black.com.myapplication.ViewModel.StepViewModel;

import static android.widget.Toast.LENGTH_LONG;
import static black.com.myapplication.AppChannel.CHANNEL_ID;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Toolbar mToolbar;
    private DrawerLayout drawer;
    StepViewModel stepViewModel;
    //local brodcast action
    public static final String LOCAL_BROADCAST_ACTION = "black.com.myapplication";
//    String notificationIntentString;


//        private LocalBroadcastManager localBroadcastManager=null;
//    private LocationBroadcastReceiver locationBroadcastReceiver=null;
    // current Location
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    //context
    public Context context;
    //floating button
    private FloatingActionButton editFloatingButton;
    private ImageView navHeaderImageView;
    private TextView navbarNameHeading, navbarSubHeading;
    View headerView;
    //switch
    SwitchCompat locationswitchCompat;
    Boolean switchState;
    NavigationView navigationView;
    //view pager
    CustomViewPager viewPager = null;

    //uri
    Uri uriImageDialog;

    //dialogImage
    Dialog dialog;
    ImageView imageNavDialogView;
    FloatingActionButton imageEditButton;
    TextView navNameHeading, navSubHeading;
    TextView okNavDialogTextView, cancleNavDialogTextView;
    String navImagePath;
    public static final String DEFAULT_NAME = "Step";
    public static final String DEFAULT_STATUS = "It is foolish to fear what we've yet to see and Know";
    public static final String DEFAULT_PATH = "N/A";

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private static final int PERMISSION_CODE_LOCATION = 1002;
    private BroadcastReceiver broadcastReceiver;
    //    private static int statusBarHeight(android.content.res.Resources res) {
//        return (int) (24 * res.getDisplayMetrics().density);
//    }
    public interface OnMenuListener {
        void getStepSelect(String from, int status);
    }

    OnMenuListener onMenuListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stepViewModel = ViewModelProviders.of(this).get(StepViewModel.class);
        stepViewModel.init();
        context = this;
        setContentView(R.layout.activity_home);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        //remove if not work transparent statusbar
//        if (Build.VERSION.SDK_INT >= 21) {
//            mToolbar.setPadding(0, statusBarHeight(getResources()), 0, 0);
//        }//use dim file insted


//        ActionBar actionBar =getSupportActionBar();
//        actionBar.setDisplayShowHomeEnabled(true);
//        actionBar.setHomeAsUpIndicator(R.drawable.baseline_menu);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        //for listening to click event in navbar
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //for display navigation view
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //to show first fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                    new ReportHomeFragment()).commit();
            navigationView.setCheckedItem(R.id.all_steps);
        }

        //for search
        handleIntent(getIntent());
        //search

        //create obj of fragment
//      NavigationDrawerFragment drawerFragment =(NavigationDrawerFragment)
//              getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
//      //in fragment.java class
//        drawerFragment.setUp(R.id.fragment_navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout),mToolbar);
        viewPager = (CustomViewPager) findViewById(R.id.pager);
        viewPager.setPagingEnabled(true);
        ZoomOutTransformation zoomOutTransformation = new ZoomOutTransformation();
         viewPager.setPageTransformer(true, zoomOutTransformation);
//        DepthTransformation depthTransformation =new DepthTransformation();
//        viewPager.setPageTransformer(true,depthTransformation);
        viewPager.setAdapter(new PagerAdapter(fragmentManager));


        // Give the TabLayout the ViewPager
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.post(new Runnable() {
            @Override
            public void run() {

                if (tabLayout.getWidth() < HomeActivity.this.getResources().getDisplayMetrics().widthPixels) {
                    tabLayout.setTabMode(TabLayout.MODE_FIXED);
                    ViewGroup.LayoutParams mParams = tabLayout.getLayoutParams();
                    mParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    tabLayout.setLayoutParams(mParams);
                    //change needed

                } else {
                    tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
                }
            }
        });

        headerView = navigationView.getHeaderView(0);
        editFloatingButton = (FloatingActionButton) headerView.findViewById(R.id.edit_floating_button);
        navHeaderImageView = (ImageView) headerView.findViewById(R.id.acc_img);
        navbarNameHeading = (TextView) headerView.findViewById(R.id.header_text);
        navbarSubHeading = (TextView) headerView.findViewById(R.id.sub_header_text);
        editFloatingButton.setImageResource(R.drawable.ic_edit_black_24dp);
        editFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(context, android.R.style.Theme_Light);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.edit_image_layout);
                dialog.show();
                imageEditButton = (FloatingActionButton) dialog.findViewById(R.id.edit_floating_button);
                imageNavDialogView = (ImageView) dialog.findViewById(R.id.image_nav_dialog);
                okNavDialogTextView = (TextView) dialog.findViewById(R.id.ok_nav_heading);
                cancleNavDialogTextView = (TextView) dialog.findViewById(R.id.cancle_nav_heading);
                navNameHeading = (TextView) dialog.findViewById(R.id.step_nav_heading1);
                navSubHeading = (TextView) dialog.findViewById(R.id.step_nav_heading2);

                imageEditButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getBaseContext(), "choose image", LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_DENIED) {
                                //permission not granted,request it
                                checkAndRequestPermissions();
                            } else {
                                //permission already granted
                                pickImageFromGallery();
                            }
                        } else {
                            //system os is less than mashmallow
                            pickImageFromGallery();
                        }


                    }
                });
                okNavDialogTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        onSaveSharedPreference();
                        onGetSharedPreference();
                        dialog.dismiss();
                    }
                });
                cancleNavDialogTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                onGetSharedPreferenceEdit();
            }
        });

        onGetSharedPreference();
        checkAndRequestPermissions();

//       //for broadcast
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(LOCAL_BROADCAST_ACTION);
//
//        localBroadcastManager=LocalBroadcastManager.getInstance(this);
//        locationBroadcastReceiver=new LocationBroadcastReceiver();
//        localBroadcastManager.registerReceiver(locationBroadcastReceiver,intentFilter);

//
//        String notificaationString=getIntent().getStringExtra(LocationBackgroundService.INTENT_KEY);
//        if(notificaationString!=null){
//            Log.d("saddasd", notificaationString);}
        Bundle extras = getIntent().getExtras();
        if(extras != null){

            String Id=extras.getString(LocationBackgroundService.INTENT_KEY);
           if (stepViewModel.updateStatus(Integer.parseInt(Id))) {
               //Ncf notificationclicked
               Intent i = new Intent(HomeActivity.this, MenuActivity.class);
               i.putExtra("Menu", "fromNotification");
               i.putExtra("NcId", Id);
               i.putExtra("NcStep", extras.getString(LocationBackgroundService.INTENT_STEP));
               i.putExtra("NcCategory", extras.getString(LocationBackgroundService.INTENT_CATEGORY));
               i.putExtra("NcPlace", extras.getString(LocationBackgroundService.INTENT_PLACE));
               startActivity(i);
           }
        }

    }

    private void pickImageFromGallery() {
        //intent to pick image
        Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        gallery.setType("image/*");
        startActivityForResult(Intent.createChooser(gallery, "Select picture"), IMAGE_PICK_CODE);
        Toast.makeText(getBaseContext(), "pick image from gallery", LENGTH_LONG).show();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//       switch (requestCode){
//           case PERMISSION_CODE:
//               if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
//                   //permission was granted
//                   pickImageFromGallery();
//               }else{
//                   //permission was denied
//                   Toast.makeText(getBaseContext(),"Permission needed to access Gallery",LENGTH_LONG).show();
//
//               }
//               break;
//           case PERMISSION_CODE_LOCATION:
//       }
//
//    }
//public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                       @NonNull int[] grantResults) {
//
//}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            //set image to image view
            Toast.makeText(getBaseContext(), "on Activity Result", LENGTH_LONG).show();
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//                  Toast.makeText(getBaseContext(), uri.getPath()+"selected image path",LENGTH_LONG).show();
                // Log.d(TAG, String.valueOf(bitmap));

                // navImagePath=getPath(getApplicationContext(),uri);
                //  navImagePath=uri.toString();
                navImagePath = getPath(uri);
                //loadImageFromSharedPreference(navImagePath);

                bitmap = getCircularBitmap(bitmap);
                imageNavDialogView.setImageBitmap(bitmap);
                navHeaderImageView.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public String getPath(Uri uri) {
//        String result=null;
//        String[] imgObj={MediaStore.Images.Media.DATA};
//        Cursor cursor =context.getContentResolver().query(uri,imgObj,null,null,null);
//        if (cursor !=null){
//            if(cursor.moveToFirst()){
//                int column_index=cursor.getColumnIndexOrThrow(imgObj[0]);
//                result = cursor.getString(column_index);
//            }
//            cursor.close();
//        }
//        if(result==null){
//            result=DEFAULT_PATH;
//
//        }
//        return result;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;

    }

    //store vale in sharedPreference
    public void onSaveSharedPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences("NavImageStatus", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nameHeading", navNameHeading.getText().toString());
        editor.putString("subHeading", navSubHeading.getText().toString());
        editor.putString("imagePath", navImagePath);
        editor.commit();

    }

    //get value from SharedPreference
    public void onGetSharedPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences("NavImageStatus", Context.MODE_PRIVATE);
        String nameHeading = sharedPreferences.getString("nameHeading", DEFAULT_NAME);
        String subHeading = sharedPreferences.getString("subHeading", DEFAULT_STATUS);
        String imageNavPath = sharedPreferences.getString("imagePath", DEFAULT_PATH);
        if (nameHeading.equals(DEFAULT_NAME) && subHeading.equals(DEFAULT_STATUS) && imageNavPath.equals(DEFAULT_PATH)) {
           //0
            navbarNameHeading.setText(DEFAULT_NAME);
            navbarSubHeading.setText(DEFAULT_STATUS);
            loadImageFromSharedPreference(DEFAULT_PATH);
            Toast.makeText(getBaseContext(), "Empty Profile", LENGTH_LONG).show();
        }
        else if (nameHeading.equals(DEFAULT_NAME) && subHeading.equals(DEFAULT_STATUS) && !imageNavPath.equals(DEFAULT_PATH)) {
            //1
            navbarNameHeading.setText(DEFAULT_NAME);
            navbarSubHeading.setText(DEFAULT_STATUS);
            loadImageFromSharedPreference(imageNavPath);
            Log.d("codeerer+++","001");
        }
        else if (nameHeading.equals(DEFAULT_NAME) && !subHeading.equals(DEFAULT_STATUS) && imageNavPath.equals(DEFAULT_PATH)) {
            //2
            navbarNameHeading.setText(DEFAULT_NAME);
            navbarSubHeading.setText(subHeading);
            loadImageFromSharedPreference(DEFAULT_PATH);
            Log.d("codeerer+++","010");
        }
        else if (nameHeading.equals(DEFAULT_NAME) && !subHeading.equals(DEFAULT_STATUS) && !imageNavPath.equals(DEFAULT_PATH)) {
            //3
            navbarNameHeading.setText(DEFAULT_NAME);
            navbarSubHeading.setText(subHeading);
            loadImageFromSharedPreference(imageNavPath);
            Log.d("codeerer+++","011");
        }
        else if (!nameHeading.equals(DEFAULT_NAME) && subHeading.equals(DEFAULT_STATUS) && imageNavPath.equals(DEFAULT_PATH)) {
           //4
            navbarNameHeading.setText(nameHeading);
            navbarSubHeading.setText(DEFAULT_STATUS);
            loadImageFromSharedPreference(DEFAULT_PATH);
            Log.d("codeerer+++","100");
        }
        else if (!nameHeading.equals(DEFAULT_NAME) && subHeading.equals(DEFAULT_STATUS) && !imageNavPath.equals(DEFAULT_PATH)) {
            //5
            navbarNameHeading.setText(nameHeading);
            navbarSubHeading.setText(DEFAULT_STATUS);
            loadImageFromSharedPreference(imageNavPath);
            Log.d("codeerer+++","101");
        }
        else if (!nameHeading.equals(DEFAULT_NAME) && !subHeading.equals(DEFAULT_STATUS) && imageNavPath.equals(DEFAULT_PATH)) {
            //6
            navbarNameHeading.setText(nameHeading);
            navbarSubHeading.setText(subHeading);
            loadImageFromSharedPreference(DEFAULT_PATH);
            Log.d("codeerer+++","110");
        }
        else {
            //7
            navbarNameHeading.setText(nameHeading);
            navbarSubHeading.setText(subHeading);
            loadImageFromSharedPreference(imageNavPath);
//            navNameHeading.setText(nameHeading);
//            navSubheading.setText(subHeading);


            Toast.makeText(getBaseContext(), imageNavPath + "Data Loaded Successfully" + nameHeading + subHeading, LENGTH_LONG).show();
        }

    }

    public void loadImageFromSharedPreference(String imageNavPath) {

        File imgFile = null;

        imgFile = new File(imageNavPath);
        if (imgFile.exists()) {
            Log.d("image+++",imageNavPath);
            Toast.makeText(getBaseContext(), imageNavPath + "inside load image path", LENGTH_LONG).show();
            Bitmap Bitmap = BitmapFactory.decodeFile(imgFile.getPath());
            Bitmap = getCircularBitmap(Bitmap);
            //imageNavDialogView.setImageBitmap(Bitmap);
            navHeaderImageView.setImageBitmap(Bitmap);
        } else {
            Toast.makeText(getBaseContext(), "No image to display", LENGTH_LONG).show();

        }
    }
    public void loadImageFromSharedPreferenceForEdit(String imageNavPath) {

        File imgFile = null;

        imgFile = new File(imageNavPath);
        if (imgFile.exists()) {
            Log.d("image+++",imageNavPath);
            Toast.makeText(getBaseContext(), imageNavPath + "inside load image path", LENGTH_LONG).show();
            Bitmap Bitmap = BitmapFactory.decodeFile(imgFile.getPath());
            Bitmap = getCircularBitmap(Bitmap);
            imageNavDialogView.setImageBitmap(Bitmap);
            //navHeaderImageView.setImageBitmap(Bitmap);
        } else {
            Toast.makeText(getBaseContext(), "No image to display", LENGTH_LONG).show();

        }
    }

    public void onGetSharedPreferenceEdit() {
        SharedPreferences sharedPreferences = getSharedPreferences("NavImageStatus", Context.MODE_PRIVATE);
        String nameHeading = sharedPreferences.getString("nameHeading", DEFAULT_NAME);
        String subHeading = sharedPreferences.getString("subHeading", DEFAULT_STATUS);
        String imageNavPath = sharedPreferences.getString("imagePath", DEFAULT_PATH);

        if (nameHeading.equals(DEFAULT_NAME) && subHeading.equals(DEFAULT_STATUS)&& imageNavPath.equals(DEFAULT_PATH)) {
            //0
            navNameHeading.setText(DEFAULT_NAME);
            navSubHeading.setText(DEFAULT_STATUS);
            loadImageFromSharedPreferenceForEdit(DEFAULT_PATH);
            Toast.makeText(getBaseContext(), "Empty Profile", LENGTH_LONG).show();
        }
        else if(nameHeading.equals(DEFAULT_NAME)&& subHeading.equals(DEFAULT_STATUS) && !imageNavPath.equals(DEFAULT_PATH)){
            //1
            navNameHeading.setText(DEFAULT_NAME);
            navSubHeading.setText(DEFAULT_STATUS);
            loadImageFromSharedPreferenceForEdit(imageNavPath);
            Log.d("code+++", " 001");
        }
        else if(nameHeading.equals(DEFAULT_NAME)&& !subHeading.equals(DEFAULT_STATUS) && imageNavPath.equals(DEFAULT_PATH)){
            //2
            navNameHeading.setText(DEFAULT_NAME);
            navSubHeading.setText(subHeading);
            loadImageFromSharedPreferenceForEdit(DEFAULT_PATH);
            Log.d("code+++", " 010");


        }
        else if(nameHeading.equals(DEFAULT_NAME)&& !subHeading.equals(DEFAULT_STATUS) && !imageNavPath.equals(DEFAULT_PATH)){
            //3
            navNameHeading.setText(DEFAULT_NAME);
            navSubHeading.setText(subHeading);
            loadImageFromSharedPreferenceForEdit(imageNavPath);
            Log.d("code+++", " 011");


        }
        else if(!nameHeading.equals(DEFAULT_NAME)&& subHeading.equals(DEFAULT_STATUS) && imageNavPath.equals(DEFAULT_PATH)){
            //4
            navNameHeading.setText(nameHeading);
            navSubHeading.setText(DEFAULT_STATUS);
            loadImageFromSharedPreferenceForEdit(DEFAULT_PATH);
            Log.d("code+++", " 100");


        }
        else if(!nameHeading.equals(DEFAULT_NAME)&& subHeading.equals(DEFAULT_STATUS) && !imageNavPath.equals(DEFAULT_PATH)){
            //5
            navNameHeading.setText(nameHeading);
            navSubHeading.setText(DEFAULT_STATUS);
            loadImageFromSharedPreferenceForEdit(imageNavPath);
            Log.d("code+++", "101");


        }
        else if(!nameHeading.equals(DEFAULT_NAME)&& !subHeading.equals(DEFAULT_STATUS) && imageNavPath.equals(DEFAULT_PATH)){
            //6
            navNameHeading.setText(nameHeading);
            navSubHeading.setText(subHeading);
            loadImageFromSharedPreferenceForEdit(DEFAULT_PATH);
            Log.d("code+++", " 110");

        }

        else {//7
            navNameHeading.setText(nameHeading);
            navSubHeading.setText(subHeading);
            loadImageFromSharedPreferenceForEdit(imageNavPath);
            Log.d("code+++", " 111");

//            navNameHeading.setText(nameHeading);
//            navSubheading.setText(subHeading);
            Toast.makeText(getBaseContext(), "Data Loaded Successfully" + nameHeading + subHeading, LENGTH_LONG).show();
        }

    }

    protected Bitmap getCircularBitmap(Bitmap srcBitmap) {
        // Calculate the circular bitmap width with border
        int squareBitmapWidth = Math.min(srcBitmap.getWidth(), srcBitmap.getHeight());

        // Initialize a new instance of Bitmap
        Bitmap dstBitmap = Bitmap.createBitmap(
                squareBitmapWidth, // Width
                squareBitmapWidth, // Height
                Bitmap.Config.ARGB_8888 // Config
        );

        /*
            Canvas
                The Canvas class holds the "draw" calls. To draw something, you need 4 basic
                components: A Bitmap to hold the pixels, a Canvas to host the draw calls (writing
                into the bitmap), a drawing primitive (e.g. Rect, Path, text, Bitmap), and a paint
                (to describe the colors and styles for the drawing).
        */
        // Initialize a new Canvas to draw circular bitmap
        Canvas canvas = new Canvas(dstBitmap);

        // Initialize a new Paint instance
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        /*
            Rect
                Rect holds four integer coordinates for a rectangle. The rectangle is represented by
                the coordinates of its 4 edges (left, top, right bottom). These fields can be accessed
                directly. Use width() and height() to retrieve the rectangle's width and height.
                Note: most methods do not check to see that the coordinates are sorted correctly
                (i.e. left <= right and top <= bottom).
        */
        /*
            Rect(int left, int top, int right, int bottom)
                Create a new rectangle with the specified coordinates.
        */
        // Initialize a new Rect instance
        Rect rect = new Rect(0, 0, squareBitmapWidth, squareBitmapWidth);

        /*
            RectF
                RectF holds four float coordinates for a rectangle. The rectangle is represented by
                the coordinates of its 4 edges (left, top, right bottom). These fields can be
                accessed directly. Use width() and height() to retrieve the rectangle's width and
                height. Note: most methods do not check to see that the coordinates are sorted
                correctly (i.e. left <= right and top <= bottom).
        */
        // Initialize a new RectF instance
        RectF rectF = new RectF(rect);

        /*
            public void drawOval (RectF oval, Paint paint)
                Draw the specified oval using the specified paint. The oval will be filled or
                framed based on the Style in the paint.

            Parameters
                oval : The rectangle bounds of the oval to be drawn

        */
        // Draw an oval shape on Canvas
        canvas.drawOval(rectF, paint);

        /*
            public Xfermode setXfermode (Xfermode xfermode)
                Set or clear the xfermode object.
                Pass null to clear any previous xfermode. As a convenience, the parameter passed
                is also returned.

            Parameters
                xfermode : May be null. The xfermode to be installed in the paint
            Returns
                xfermode
        */
        /*
            public PorterDuffXfermode (PorterDuff.Mode mode)
                Create an xfermode that uses the specified porter-duff mode.

            Parameters
                mode : The porter-duff mode that is applied

        */
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // Calculate the left and top of copied bitmap
        float left = (squareBitmapWidth - srcBitmap.getWidth()) / 2;
        float top = (squareBitmapWidth - srcBitmap.getHeight()) / 2;

        /*
            public void drawBitmap (Bitmap bitmap, float left, float top, Paint paint)
                Draw the specified bitmap, with its top/left corner at (x,y), using the specified
                paint, transformed by the current matrix.

                Note: if the paint contains a maskfilter that generates a mask which extends beyond
                the bitmap's original width/height (e.g. BlurMaskFilter), then the bitmap will be
                drawn as if it were in a Shader with CLAMP mode. Thus the color outside of the

                original width/height will be the edge color replicated.

                If the bitmap and canvas have different densities, this function will take care of
                automatically scaling the bitmap to draw at the same density as the canvas.

            Parameters
                bitmap : The bitmap to be drawn
                left : The position of the left side of the bitmap being drawn
                top : The position of the top side of the bitmap being drawn
                paint : The paint used to draw the bitmap (may be null)
        */
        // Make a rounded image by copying at the exact center position of source image
        canvas.drawBitmap(srcBitmap, left, top, paint);

        // Free the native object associated with this bitmap.
        srcBitmap.recycle();

        // Return the circular bitmap
        return dstBitmap;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }//for search

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem mSearch = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;
        if (mSearch != null) {
            searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        }
        // Assumes current activity is the searchable activity
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        }
        return true;
    }

    //for search
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    //for search
    private void doMySearch(String query) {
        stepViewModel.sendVmSearch("%" + query + "%");
        Toast.makeText(this, "%" + query + "%", LENGTH_LONG).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.all_steps:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                new StepHomeFragment()).commit();
                //onMenuListener.getStepSelect("All Step", 1);
                stepViewModel.sendStatus("All Step");
                //Toast.makeText(this,"All Step",LENGTH_LONG).show();
                break;
            case R.id.by_complete:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                        new StepHomeFragment()).commit();
                // onMenuListener.getStepSelect("Completed",2);
                stepViewModel.sendStatus("Completed");
                // Toast.makeText(this,"Completed",LENGTH_LONG).show();
                break;
            case R.id.by_notComplete:
                //onMenuListener.getStepSelect("Not completed",3);
                stepViewModel.sendStatus("NotCompleted");
                //  Toast.makeText(this,"NotComplete",LENGTH_LONG).show();
                break;
            case R.id.location_toggle:

//                Menu menu = navigationView.getMenu();
//                MenuItem menuItem = menu.findItem(R.id.location_toggle);

//                View actionView = MenuItemCompat.getActionView(item);
//
//                locationswitchCompat = (SwitchCompat) actionView.findViewById(R.id.location_switch);
//                locationswitchCompat.setChecked(true);
//                locationswitchCompat.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Snackbar.make(v, (locationswitchCompat.isChecked()) ? "is checked!!!" : "not checked!!!", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
//                    }
//                });
                item.setEnabled(false);
                final boolean[] isTouched = {false};

                locationswitchCompat = (SwitchCompat) findViewById(R.id.location_switch);
                locationswitchCompat.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        isTouched[0] = true;
                        return false;
                    }
                });
                locationswitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            if (checkAndRequestPermissions()) {
                                stepViewModel.sendStatus("LocationToggle");
                                Toast.makeText(getBaseContext(), "Turn on Location", LENGTH_LONG).show();
                            } else{
                                locationswitchCompat.setChecked(false);
                            }
                        } else {
                            stepViewModel.sendStatus("LocationToggleOff");
                            Intent service =new Intent(getApplicationContext(), LocationBackgroundService.class);
                            stopService(service);
                            Toast.makeText(getBaseContext(), "Turn off Location", LENGTH_LONG).show();
                        }
                    }
                });
                break;
            case R.id.tips:
                //stepViewModel.sendStatus("Tips");

                Intent i = new Intent(HomeActivity.this, MenuActivity.class);
                i.putExtra("Menu", "tips");
                startActivity(i);

                break;
            case R.id.reset:
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Reset All Steps");
                builder.setCancelable(false);
                builder.setMessage(getResources().getString(R.string.resetMessage));
                builder.setPositiveButton("Reset All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stepViewModel.sendStatus("ResetAll");
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(HomeActivity.this, "Cancel button Clicked!", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });
                builder.show();
                break;
            case R.id.setting:
                Intent i1 = new Intent(HomeActivity.this, MenuActivity.class);
                i1.putExtra("Menu", "setting");
                startActivity(i1);

                break;
            case R.id.about_steps:
                Intent i2 = new Intent(HomeActivity.this, MenuActivity.class);
                i2.putExtra("Menu", "aboutSteps");
                startActivity(i2);
                //stepViewModel.sendStatus("AboutStep");
                break;
            default:
                break;

        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private boolean checkAndRequestPermissions() {
        int loc = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int loc2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int storageRead = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int storageWrite = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (storageWrite != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (storageRead != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (REQUEST_ID_MULTIPLE_PERMISSIONS == 1) {
//            for (int i = 0; i < grantResults.length; i++) {
//                if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED) {
//                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//                    }
//                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                            == PackageManager.PERMISSION_GRANTED) {
//                    }
//                }
//
//            }
//        }
//    }


    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver!=null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Intent intent1 = new Intent(getApplicationContext(),HomeActivity.class);
                    PendingIntent pendingIntent =PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_place_black_24dp)
                            .setContentTitle("places")
                            .setContentText("You Reached your Destination")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent)
                            .build();
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(CHANNEL_ID,199751994,notification);
                }
            };
        }
        Log.d("avsj","onresumr");
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            String data1 = extras.getString(LocationBackgroundService.INTENT_KEY);
            Log.d("asdasd",data1);
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
//        LocalBroadcastManager.getInstance(this).registerReceiver(
//                locationBroadcastReceiver, new IntentFilter(LOCAL_BROADCAST_ACTION));
    }

//    @Override
//    protected void onPause() {
//        LocalBroadcastManager.getInstance(this).registerReceiver(
//                locationBroadcastReceiver, new IntentFilter());
//        super.onPause();
//
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver!=null){
            unregisterReceiver(broadcastReceiver);
        }
//       if(localBroadcastManager!=null){
//                localBroadcastManager.unregisterReceiver(locationBroadcastReceiver);
//        }
    }
}

//TODO:add fragments and in fragments add recycler view
