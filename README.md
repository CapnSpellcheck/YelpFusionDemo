# YelpFusionDemo

An overview of the decisions I've made and full disclosure of imperfections.

I set a min SDK level of 21. There were no explicit requirements. The choice of 21 keeps development
and testing simple due to the consistency of features provided since that Android release, such as
material design. For a real app, I'd *probably* add support for 19. However, that adds quite a bit
of time to dev and testing, since more significant changes come into play. Heck, for my own app,
starting from a user base of zero I chose to support 21 up front and possibly extend to 19 later.

I'm using Universal Image Loader library. I chose this because I'm quite familiar with it and started
using it in 2016, and it's a real feature-rich image manager. Unfortunately, it's no longer maintained.
If I had to choose another more current library, I'd choose to become familiar with Picasso.

The description didn't mention inputting location, so I chose to hardcode the location to part of Manhattan.

### Deviations from "the perfect submission" that I am aware of

This warning can appear in Logcat:
W/RecyclerView: Cannot call this method in a scroll callback. Scroll callbacks mightbe run during a measure & layout pass where you cannot change theRecyclerView data. Any method call that might change the structureof the RecyclerView or the adapter contents should be postponed tothe next frame.

This is because I insert a new item into the adapter during the scroll callback. Note that it's very unlikely that
a layout pass is taking place when this code is hit. (Although, I have seen an occasional crash associated
with this loading indicator implementation in my own app.)
There were other caveats that came with deferring that callback via posting. Although it should be possible to fix it going that route,
it hasn't been a priority yet in my app from which this code came.

There is a style mismatch in the search history list on API 25 and up (or maybe 24).
On 23 and below, the style matches the action bar -- light text on dark background. On 25, for some
reason it uses a light background. I haven't had the time to investigate.

The first time you tap on a grid item after entering the search query, the keyboard can pop up again.
The Android input focus system has its surprising moments.