package liuyibo.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyibo on 16-3-4.
 */
public class ListBuilder<T> {

    private List<T> mList;
    private List<T> mRes;

    public ListBuilder() {
        mList = new ArrayList<>();
    }

    public ListBuilder(List<T> list) {
        mList = new ArrayList<>();
        resource(list);
    }

    public ListBuilder<T> resource(List<T> list) {
        mRes = list;
        return this;
    }

    public ListBuilder<T> append(T item) {
        mList.add(item);
        return this;
    }

    public ListBuilder<T> append(int start, int end) {
        if (start >= end) {
            return this;
        }
        mList.addAll(mRes.subList(start, end));
        return this;
    }

    public ListBuilder<T> append(int start) {
        return append(start, mRes.size());
    }

    public int length() {
        return mList.size();
    }

    public List<T> build() {
        return mList;
    }
}
