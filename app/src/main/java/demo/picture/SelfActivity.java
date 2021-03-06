package demo.picture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cwf.app.cwf.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import demo.picture.toolbox.BitmapTemp;
import demo.picture.toolbox.GalleryActivity;
import demo.picture.toolbox.ImageFileActivity;
import lib.utils.ActivityUtils;
import demo.picture.toolbox.entiy.ImageItem;
import lib.BaseActivity;
import lib.utils.FileUtils;
import lib.widget.GridAdapter;
import lib.widget.ViewHolder;

/**
 * Created by n-240 on 2015/9/28.
 */
public class SelfActivity extends BaseActivity {

    private static final int TAKE_CAMERA = 0x000001;
    private static final int TAKE_PHOTOS = 0x000002;
    private static final int VIEW_PHOTOS = 0x000003;

    private GridView noScrollGridView;
    private GridAdapter<ImageItem> mAdapter;
    public static int Max = 20;
    public static Bitmap bitmap;
    private LinearLayout ll_popup;

    private PopupWindow popupWindow;

    private View parentView;

    public static ArrayList<ImageItem> listImageItem = new ArrayList<ImageItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentView = getLayoutInflater().inflate(R.layout.layout_gridview, null);
        setContentView(parentView);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_addpic_unfocused);
        init();
    }

    private void init() {
        popupWindow = new PopupWindow(this);

        View view = getLayoutInflater().inflate(R.layout.item_popupwindows, null);

        ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);

        popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setContentView(view);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        //因为某些机型是虚拟按键的,所以要加上以下设置防止挡住按键.
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.parent);
        parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                popupWindow.dismiss();
                ll_popup.clearAnimation();
            }
        });

        Button photos = (Button) view.findViewById(R.id.item_popupwindows_Photo);
        photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SelfActivity.this, ImageFileActivity.class);
                startActivity(i);
                popupWindow.dismiss();
                ll_popup.clearAnimation();
            }
        });

        Button camera = (Button) view.findViewById(R.id.item_popupwindows_camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera();
                popupWindow.dismiss();
                ll_popup.clearAnimation();
            }
        });

        Button cancel = (Button) view.findViewById(R.id.item_popupwindows_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ll_popup.clearAnimation();
            }
        });

        noScrollGridView = (GridView) findViewById(R.id.noScrollgridview);
        noScrollGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = initAdapter();
        noScrollGridView.setAdapter(mAdapter);
        noScrollGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (position == BitmapTemp.tempSelectBitmap.size()) {
                    ll_popup.startAnimation(AnimationUtils.loadAnimation(SelfActivity.this,
                            R.anim.abc_fade_in));
                    popupWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
                } else {
                    GalleryActivity.startThisActivity(SelfActivity.this, BitmapTemp.tempSelectBitmap, position);
                }
            }
        });

    }

    private File cameraFile = null;

    /*
    * 打开相机
    * */
    public void camera() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String fileName = String.valueOf(System.currentTimeMillis());

        cameraFile = new File(FileUtils.getInstance(this).photoCache, fileName + ".jpg");
        if (cameraFile.exists()) {
            cameraFile.delete();
        }
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
        startActivityForResult(openCameraIntent, TAKE_CAMERA);

    }


    /*
    * activity回调
    * */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_CAMERA:
                if (resultCode != RESULT_OK) {
                    cameraFile.delete();
                    cameraFile = null;
                }
                if (canAddPhotos() && cameraFile != null) {
                    ImageItem takePhoto = new ImageItem();
                    takePhoto.setImageId(BitmapTemp.tempSelectBitmap.size() + "");
                    takePhoto.setImagePath(cameraFile.getAbsolutePath());
                    BitmapTemp.tempSelectBitmap.add(takePhoto);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case TAKE_PHOTOS:
                break;
            case VIEW_PHOTOS:
                break;
        }
    }


    /**
     * item 适配
     *
     * @return
     */
    private GridAdapter<ImageItem> initAdapter() {
        GridAdapter<ImageItem> mAdapter = new GridAdapter<ImageItem>(this, R.layout.item_grid_photos,
                BitmapTemp.tempSelectBitmap) {


            @Override
            public void buildView(ViewHolder holder, ImageItem data) {
                Glide.with(SelfActivity.this)
                        .load(data.getThumbnailPath())
                        .error(R.drawable.error)
//                        .placeholder(R.drawable.loading4)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into((ImageView) holder.findViewById(R.id.item_grid_image));
            }

            @Override
            public void buildAddView(ViewHolder holder) {
                Glide.with(SelfActivity.this)
                        .load(R.drawable.icon_addpic_unfocused)
                        .error(R.drawable.error)
//                        .placeholder(R.drawable.loading4)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .fitCenter()
                        .into((ImageView) holder.findViewById(R.id.item_grid_image));
            }


            @Override
            public boolean canAddItem() {
                return true;
            }

            @Override
            public int getMaxSize() {
                return Max;
            }

        };
        return mAdapter;
    }

    public static boolean canAddPhotos() {
        if (Max == -1 || BitmapTemp.tempSelectBitmap.size() < Max)
            return true;
        else {
            ActivityUtils.showTip("最多添加" + Max + "张照片", false);
            return false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_submit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_id_submit).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_id_submit) {
            ll_popup.startAnimation(AnimationUtils.loadAnimation(SelfActivity.this,
                    R.anim.fade_in));
            popupWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }
}
