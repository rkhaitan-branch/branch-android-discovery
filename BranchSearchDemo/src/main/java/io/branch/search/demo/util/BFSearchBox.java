package io.branch.search.demo.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import io.branch.search.demo.R;


/**
 * Created by sojanpr on 10/29/15.
 * <p/>
 * Class creates a Search control with text area and search button. Update the suggestions as user types
 * and provide option to update keywords
 */
@SuppressWarnings("unused")
public class BFSearchBox extends android.support.v7.widget.AppCompatAutoCompleteTextView {
    private ArrayList<String> searchSuggestions_;
    Context context_;
    IKeywordChangeListener keywordChangeListener_;

    public BFSearchBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        context_ = context;
        initBranchSearchBox();
    }

    public BFSearchBox(Context context) {
        super(context);
        context_ = context;
        initBranchSearchBox();
    }

    public BFSearchBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        context_ = context;
        initBranchSearchBox();
    }


    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void initBranchSearchBox() {
        addTextChangedListener(txtWatcher);
        this.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        this.setSingleLine(true);
        //noinspection deprecation
        setBackgroundColor(getResources().getColor(R.color.bf_text_off_white));
    }


    public void setSearchButton(View searchButton) {
        searchButton.setOnClickListener(new OnClickListener() {
            @TargetApi(Build.VERSION_CODES.CUPCAKE)
            @Override
            public void onClick(View v) {

                if (BFSearchBox.this.getText().toString().length() > 0) {
                    ArrayList<String> searchKeys = new ArrayList<>();
                    if (keywordChangeListener_ != null) {
                        keywordChangeListener_.onKeywordChanged(BFSearchBox.this.getText().toString());
                    }

                    InputMethodManager imm = (InputMethodManager) context_.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(BFSearchBox.this.getWindowToken(), 0);
                    }
                } else {
                    if (BFSearchBox.this.getVisibility() == View.VISIBLE) {
                        BFSearchBox.this.setText("");
                        BFSearchBox.this.setVisibility(View.GONE);
                        if (keywordChangeListener_ != null) {
                            keywordChangeListener_.onSearchBoxClosed();
                        }
                        InputMethodManager imm = (InputMethodManager) context_.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(BFSearchBox.this.getWindowToken(), 0);
                        }
                    } else {
                        BFSearchBox.this.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

    }

    public void setKeywordChangeListener(IKeywordChangeListener listener) {
        keywordChangeListener_ = listener;
    }

    public void updateSearchSuggestions(ArrayList<String> suggestions) {
        searchSuggestions_ = suggestions;

        ArrayAdapter adapter = new ArrayAdapter<>(context_,
                android.R.layout.simple_list_item_1, searchSuggestions_.toArray(new String[searchSuggestions_.size()]));
        this.setAdapter(adapter);
    }

    public ArrayList<String> getSearchSuggestions() {
        return searchSuggestions_;
    }

    public String getSearchSuggestion(int position) {
        return searchSuggestions_.get(position);
    }

    TextWatcher txtWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
                BFSearchBox.this.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            } else {
                BFSearchBox.this.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (keywordChangeListener_ != null) {
                keywordChangeListener_.onKeywordChanged(BFSearchBox.this.getText().toString());
            }
        }
    };

    public interface IKeywordChangeListener {
        void onKeywordChanged(String keyword);

        void onSearchBoxClosed();
    }


    private class suggestionAdapter extends BaseAdapter implements Filterable {

        @Override
        public int getCount() {
            return searchSuggestions_.size();
        }

        @Override
        public Object getItem(int position) {
            return searchSuggestions_.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflator = LayoutInflater.from(context_);
                view = inflator.inflate(android.R.layout.simple_list_item_1, null);
            }
            ((TextView) view.findViewById(android.R.id.text1)).setText(searchSuggestions_.get(position));
            return view;
        }

        @Override
        public Filter getFilter() {
            return null;
        }
    }
}
