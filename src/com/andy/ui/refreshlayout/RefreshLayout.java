package com.andy.ui.refreshlayout;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Scroller;

/*
 * 一个自定义的VIEW控件，可以包含一个头部布局和一个listView布局，上拉可以刷新，下拉可以隐藏头部布局,显示标题
 */
public class RefreshLayout extends ViewGroup {
	private static final String TAG = RefreshLayout.class.getSimpleName();

	static Activity mContext;
	Scroller mScroller;
	LinearLayout mHeader;// 头部布局
	ProgressBar mProgressBar;
	View mTitleHeader;// 标题布局
	ListView mListView;// 显示内容的listview

	boolean isPullingDown = false;
	boolean isInit = true;

	int density;
	int touchSlop;

	float lastX;
	float lastY;

	float dy;

	RefreshListener refreshListener;
	boolean isRefreshing;

	boolean isPullingUp;
	LoadListener loadListener;
	boolean isLoading;

	public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		Log.e(TAG, "defstyleattr constructor.");
		mContext = (Activity) context;
		init();
	}

	public RefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = (Activity) context;
		Log.e(TAG, "attr constructor.");
		init();
	}

	public RefreshLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		Log.e(TAG, "constructor.");
		mContext = (Activity) context;
		init();
	}

	/**
	 * 获取手机的Density
	 */
	public static int getDisplayDensity() {
		DisplayMetrics metric = new DisplayMetrics();
		mContext.getWindowManager().getDefaultDisplay().getMetrics(metric);

		return metric.densityDpi;
	}

	private void init() {
		density = getDisplayDensity();
		touchSlop = ViewConfiguration.getTouchSlop() * density;

		mScroller = new Scroller(mContext);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();

		initChild();
	}

	private void initChild() {
		Log.e(TAG, "init");
		mHeader = (LinearLayout) this.getChildAt(0);
		mTitleHeader = this.getChildAt(1);
		mListView = (ListView) this.getChildAt(2);

		mProgressBar = (ProgressBar) mHeader.getChildAt(0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Log.e(TAG, "onMeasure");
		int measuredWidth = this.getMeasuredWidth();
		int measuredHeight = this.getMeasuredHeight();
		this.measureChildren(widthMeasureSpec, heightMeasureSpec);

		Log.e(TAG, "width=" + measuredWidth + ",height=" + measuredHeight);
		this.setMeasuredDimension(measuredWidth, measuredHeight);

		// mHeader = this.getChildAt(0);
		// mTitleHeader = this.getChildAt(1);
		// mListView = (ListView) this.getChildAt(2);

		if (isInit) {
			mTitleHeader.setVisibility(View.GONE);
			mHeader.setVisibility(View.VISIBLE);

			// 计算listview的具体长度,并设置
			int listviewHeight = measuredHeight - mHeader.getMeasuredHeight();
			int listviewHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
					listviewHeight, MeasureSpec.EXACTLY);
			mListView.measure(widthMeasureSpec, listviewHeightMeasureSpec);

		} else {
			mTitleHeader.setVisibility(View.VISIBLE);
			mHeader.setVisibility(View.GONE);

			int listviewHeight = measuredHeight
					- mTitleHeader.getMeasuredHeight();
			int listviewHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
					listviewHeight, MeasureSpec.EXACTLY);
			mListView.measure(widthMeasureSpec, listviewHeightMeasureSpec);
		}

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onLayout");
		int tempHeight = (int) dy;
		int count = this.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = this.getChildAt(i);
			if (child.getVisibility() == View.VISIBLE) {
				int measuredWidth = child.getMeasuredWidth();
				int measuredHeight = child.getMeasuredHeight();
				Log.e(TAG, "child" + i + " width=" + measuredWidth + ",height="
						+ measuredHeight);
				child.layout(0, tempHeight, measuredWidth, tempHeight
						+ measuredHeight);

				tempHeight += measuredHeight;
			}
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		Log.e(TAG, "dispatchTouchEvent");

		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.e(TAG,
				"onInterceptTouchEvent " + mListView.getFirstVisiblePosition());

		if (mListView.getFirstVisiblePosition() == 0) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Log.e(TAG, "down");
				lastX = event.getX();
				lastY = event.getY();
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				Log.e(TAG, "move y=" + event.getY() + ",lastY=" + lastY);
				if (event.getY() > lastY) {
					isPullingDown = true;
					return true;
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				return isPullingDown;
			}
		} else if (mListView.getLastVisiblePosition() == mListView.getCount() - 1) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Log.e(TAG, "down");
				lastX = event.getX();
				lastY = event.getY();
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				Log.e(TAG, "move y=" + event.getY() + ",lastY=" + lastY);
				if (event.getY() < lastY) {
					isPullingUp = true;
					return true;
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				return isPullingUp;
			}
		} else {
			isPullingDown = false;
			isPullingUp = false;
			return false;
		}

		return super.onInterceptTouchEvent(event);
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onTouchEvent");

		if (isPullingDown) {
			if (event.getAction() == MotionEvent.ACTION_MOVE) {//手势滑动
				Log.e(TAG, "move y=" + event.getY() + ",lastY=" + lastY
						+ ",scrolly=" + this.getScrollY());
				if (event.getY() > lastY) {
					dy += (int) (event.getY() - lastY);

					//界面随着手势下滑
					requestLayout();

					Log.e(TAG, "dy=" + dy);
					this.invalidate();

					isPullingDown = true;

					lastX = event.getX();
					lastY = event.getY();
					return false;
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {//放手后

				//利用属性动画 将界面拉回到原来的位置  展现一个弹性的效果
				ValueAnimator anim = ValueAnimator.ofFloat(dy, 0f);
				anim.setDuration((long) (dy / 10));
				anim.setInterpolator(new AccelerateDecelerateInterpolator());
				anim.addUpdateListener(new AnimatorUpdateListener() {

					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						float dy1 = (Float) animation.getAnimatedValue();
						dy = dy1;
						requestLayout();

					}

				});
				anim.start();

				if (refreshListener != null && !isRefreshing) {
					setRefreshState(true);

					refreshListener.onRefresh();
				}

				isPullingDown = false;
			}

			return false;
		}

		if (isPullingUp) {
			if (event.getAction() == MotionEvent.ACTION_UP) {

				if (loadListener != null && !isLoading) {
					setLoadState(true);

					loadListener.onLoad();
				}

				isPullingUp = false;
			}

			return false;
		}

		return false;

	}

	public void setLoadState(boolean state) {
		isLoading = state;
	}

	public void setRefreshState(boolean state) {
		isRefreshing = state;
		mProgressBar.setVisibility(state ? View.VISIBLE : View.GONE);
		this.invalidate();
	}

	public void setAdapter(BaseAdapter adapter) {
		Log.e(TAG, "list view=" + mListView);
		mListView.setAdapter(adapter);
	}

	public void setRefreshListener(RefreshListener listener) {
		refreshListener = listener;
	}

	public void setLoadListener(LoadListener listener) {
		loadListener = listener;
	}

	public interface RefreshListener {
		public void onRefresh();

		public void onComplete();
	}

	public interface LoadListener {
		public void onLoad();

		public void onComplete();
	}

}
