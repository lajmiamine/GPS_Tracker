package com.icare.mal21.astertech;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class HeaderDecoration extends RecyclerView.ItemDecoration {

    private View mLayout;
    ArrayList<String> genres = new ArrayList<>();
    ArrayList<Integer> genresNbr = new ArrayList<>();

    public HeaderDecoration(final Context context, RecyclerView parent, @LayoutRes int resId, ArrayList<String> genres, ArrayList<Integer> genresNbr) {
        // inflate and measure the layout
        mLayout = LayoutInflater.from(context).inflate(resId, parent, false);
        mLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        this.genres=genres;
        this.genresNbr=genresNbr;
    }


    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        // layout basically just gets drawn on the reserved space on top of the first view

        mLayout.layout(parent.getLeft(), 0, parent.getRight(), mLayout.getMeasuredHeight());
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (parent.getChildAdapterPosition(view) == 0) {
                c.save();
                final int height = mLayout.getMeasuredHeight();
                final int top = view.getTop() - height;
                c.translate(0, top);
                mLayout.draw(c);
                TextView tv = (TextView) mLayout.findViewById(R.id.headerTextView);
                tv.setText("New Section");
                c.restore();
                break;
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.set(0, mLayout.getMeasuredHeight(), 0, 0);
        } else {
            outRect.setEmpty();
        }
    }
}