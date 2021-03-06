
package com.honey.text;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button but_qq;
    private Tencent mtencent;
    private static final String TAG="MainActivity";
    private static final String App_ID="1105602574";//官方获取的APPID
    private BaseUiListener baseUiListener;
    private UserInfo mUserInfo;
    //网名
    private TextView QQname;
    //QQ头像
    private ImageView QQhead;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化控件
        initView();
    }
    /**
     * 获取空件
     */
    private void initView() {
        //QQ第三登录 //传入参数APPID和全局Context上下文
        mtencent=Tencent.createInstance(App_ID,MainActivity.this.getApplicationContext());
        QQname= (TextView) findViewById(R.id.text_view);
        QQhead= (ImageView) findViewById(R.id.image);
        //因为要获取头像所以我们用ImageLoader来获取
        DisplayImageOptions displayImageOptions=new DisplayImageOptions.Builder()
                .build();
        ImageLoaderConfiguration configuration=new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(displayImageOptions)
                .build();
        //记住要将configuration存入到ImageLoader中
        ImageLoader.getInstance().init(configuration);
        //点击QQ
        but_qq= (Button) findViewById(R.id.but_qq);
        but_qq.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
    /**通过这句代码，SDK实现了QQ的登录，这个方法有三个参数，第一个参数是context上下文，第二个参数SCOPO 是一个String类型的字符串，表示一些权限
        官方文档中的说明：应用需要获得哪些API的权限，由“，”分隔。例如：SCOPE = “get_user_info,add_t”；所有权限用“all”
         第三个参数，是一个事件监听器，IUiListener接口的实例，这里用的是该接口的实现类 */
        baseUiListener=new BaseUiListener();
        //all表示获取所有权限
        mtencent.login(MainActivity.this,"all",baseUiListener);
    }
    /**
     * 自定义监听器实现IUiListener接口后，需要实现的3个方法
     * onComplete完成 onError错误 onCancel取消
     */
    public class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object o) {
            Toast.makeText(MainActivity.this,"授权成功！",Toast.LENGTH_SHORT).show();;
            Log.e(TAG,"response:"+o);
            JSONObject object= (JSONObject) o;
            try {
                String openID = object.getString("openid");
                String accessToken = object.getString("access_token");
                String expires = object.getString("expires_in");
                mtencent.setOpenId(openID);
                mtencent.setAccessToken(accessToken,expires);
                QQToken qqToken = mtencent.getQQToken();
                mUserInfo = new UserInfo(MainActivity.this,qqToken);
                mUserInfo.getUserInfo(new IUiListener() {
                    @Override
                    public void onComplete(Object response) {
                        Log.e(TAG,"登录成功"+response.toString());
                        System.out.println("=====res"+response);
                        //将网名和头像解析出来
                        JSONObject object= (JSONObject) response;
                        try {
                            //获取网名
                            String name=object.getString("nickname");
                            //加载网名
                            QQname.setText(name);
                            //打印网名
                            System.out.println("===网名"+QQname);
                            //获取头像
                            String url=object.getString("figureurl_qq_2");
                            //打印url
                            System.out.println("======="+url);
                            //打印头像
                            System.out.println("===头像"+QQhead);
                            //加载头像
                            ImageLoader.getInstance().displayImage(url,QQhead);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(UiError uiError) {
                        Log.e(TAG,"登录失败"+uiError.toString());
                    }
                    @Override
                    public void onCancel() {
                        Log.e(TAG,"登录取消");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(UiError uiError) {
            Toast.makeText(MainActivity.this, "授权失败", Toast.LENGTH_SHORT).show();

        }
        @Override
        public void onCancel() {
            Toast.makeText(MainActivity.this, "授权取消", Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.REQUEST_LOGIN){
            Tencent.onActivityResultData(requestCode,resultCode,data,baseUiListener);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
