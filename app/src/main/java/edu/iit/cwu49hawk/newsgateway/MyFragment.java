package edu.iit.cwu49hawk.newsgateway;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

public class MyFragment extends Fragment {
    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    public static final MyFragment newInstance(Article message)
    {
        MyFragment f = new MyFragment();
        Bundle bdl = new Bundle(1);
        bdl.putParcelable(EXTRA_MESSAGE, message);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Article message = getArguments().getParcelable(EXTRA_MESSAGE);
        View v = inflater.inflate(R.layout.myfragment_layout, container, false);
        TextView title= (TextView)v.findViewById(R.id.headline);
        TextView description= (TextView)v.findViewById(R.id.description);
        TextView publishedAt= (TextView)v.findViewById(R.id.publishedAt);
        TextView count = (TextView)v.findViewById(R.id.pageCount);
        final ImageView tphotoUrl= (ImageView)v.findViewById(R.id.photoUrl);
        final String photoUrl = message.getUrlToImage();

        if (message.getPublishedAt() != "" || message.getPublishedAt() != null) {
            Picasso picasso = new Picasso.Builder(getActivity()).listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
// Here we try https if the http image attempt failed
                    final String changedUrl = photoUrl.replace("http:", "https:");
                    picasso.load(changedUrl)
                            .error(R.drawable.brokenimage)
                            .placeholder(R.drawable.placeholder)
                            .fit()
                            .into(tphotoUrl);
                }
            }).build();
            picasso.load(photoUrl)
                    .error(R.drawable.brokenimage)
                    .placeholder(R.drawable.placeholder)
                    .fit()
                    .into(tphotoUrl);
        } else {
            Picasso.with(getActivity()).load(photoUrl)
                    .error(R.drawable.brokenimage)
                    .placeholder(R.drawable.missingimage)
                    .fit()
                    .into(tphotoUrl);
        }

        title.setText(message.getTitle());
        description.setText(message.getDescription());
        publishedAt.setText(message.getPublishedAt().substring(0,10));
        //count.setText(message.getTitle());

        return v;
    }

}