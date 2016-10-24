package com.wingjay.jianshi.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.wingjay.jianshi.R;
import com.wingjay.jianshi.db.service.DiaryService;
import com.wingjay.jianshi.global.JianShiApplication;
import com.wingjay.jianshi.prefs.UserPrefs;
import com.wingjay.jianshi.ui.base.BaseActivity;
import com.wingjay.jianshi.ui.widget.MultipleRowTextView;
import com.wingjay.jianshi.ui.widget.RedPointView;
import com.wingjay.jianshi.util.LanguageUtil;

import javax.inject.Inject;

import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ViewActivity extends BaseActivity {

  @InjectView(R.id.view_edit)
  RedPointView edit;

  @InjectView(R.id.hori_container)
  ScrollView verticalScrollView;

  @InjectView(R.id.view_title)
  TextView horizTitle;

  @InjectView(R.id.view_content)
  TextView horizContent;

  @InjectView(R.id.vertical_container)
  HorizontalScrollView horizontalScrollView;

  @InjectView(R.id.vertical_view_title)
  MultipleRowTextView verticalTitle;

  @InjectView(R.id.vertical_view_content)
  MultipleRowTextView verticalContent;

  private String diaryUuid;
  private boolean verticalStyle = false;

  @Inject
  DiaryService diaryService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view);
    JianShiApplication.getAppComponent().inject(this);
    UserPrefs userPrefs = new UserPrefs(ViewActivity.this);
    verticalStyle = userPrefs.getVerticalWrite();
    setVisibilityByVerticalStyle();

    diaryUuid = getIntent().getStringExtra(EditActivity.DIARY_UUID);
    if (diaryUuid == null) {
      finish();
    }

    loadDiary();

    edit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setResult(RESULT_OK);
        Intent i = EditActivity.createIntentWithId(ViewActivity.this, diaryUuid);
        startActivity(i);
        finish();
      }
    });

    Handler handler = new HorizHandler(horizontalScrollView, verticalContent);
    verticalContent.setHandler(handler);
  }

  private void loadDiary() {
    diaryService.getDiaryByUuid(diaryUuid)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<com.wingjay.jianshi.db.model.Diary>() {
          @Override
          public void call(com.wingjay.jianshi.db.model.Diary diary) {
            if (diary != null) {
              showDiary(diary.getTitle(),
                  diary.getContent()
                      + LanguageUtil.getDiaryDateEnder(
                      getApplicationContext(),
                      diary.getTime_created()));
            }
          }
        });
  }

  private static class HorizHandler extends Handler {

    private static HorizontalScrollView MhorizontalScrollView;
    private static MultipleRowTextView mMultipleRowTextView;

    public HorizHandler(HorizontalScrollView horizontalScrollView,
                        MultipleRowTextView multipleRowTextView) {
      MhorizontalScrollView = horizontalScrollView;
      mMultipleRowTextView = multipleRowTextView;
    }

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case MultipleRowTextView.LAYOUT_CHANGED:
          MhorizontalScrollView.scrollBy(mMultipleRowTextView.getTextWidth(), 0);
          break;
      }
    }
  }

  private void showDiary(String titleString, String contentString) {
    setVisibilityByVerticalStyle();

    if (verticalStyle) {
      verticalTitle.setText(titleString);
      verticalContent.setText(contentString);
    } else {
      horizTitle.setText(titleString);
      horizContent.setText(contentString);
    }
  }

  private void setVisibilityByVerticalStyle() {
    verticalScrollView.setVisibility(verticalStyle ? View.GONE : View.VISIBLE);
    horizontalScrollView.setVisibility(verticalStyle ? View.VISIBLE : View.GONE);
  }

  public static Intent createIntent(Context context, String diaryId) {
    Intent i = new Intent(context, ViewActivity.class);
    i.putExtra(EditActivity.DIARY_UUID, diaryId);
    return i;
  }

}
