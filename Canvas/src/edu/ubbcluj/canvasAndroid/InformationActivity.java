package edu.ubbcluj.canvasAndroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import edu.ubbcluj.canvasAndroid.backend.repository.AnnouncementDAO;
import edu.ubbcluj.canvasAndroid.backend.repository.AssignmentsDAO;
import edu.ubbcluj.canvasAndroid.backend.repository.DAOFactory;
import edu.ubbcluj.canvasAndroid.backend.repository.restApi.RestInformationDAO;
import edu.ubbcluj.canvasAndroid.backend.util.CourseProvider;
import edu.ubbcluj.canvasAndroid.backend.util.PropertyProvider;
import edu.ubbcluj.canvasAndroid.backend.util.informListener.InformationEvent;
import edu.ubbcluj.canvasAndroid.backend.util.informListener.InformationListener;
import edu.ubbcluj.canvasAndroid.model.Announcement;
import edu.ubbcluj.canvasAndroid.model.Assignment;

public class InformationActivity extends BaseActivity {

	private enum ActivityType {
		Assignment, Announcement
	};

	public static final ActivityType AssignmentInformation = ActivityType.Assignment;
	public static final ActivityType AnnouncementInformation = ActivityType.Announcement;

	private static ActivityType activityType;
	private static int courseID;
	private static int assignmentID;
	private static int announcementID;
	
	private static String activityTitle = "Information";

	private static View progressContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Bundle bundle = getIntent().getExtras();

		activityType = (ActivityType) bundle.getSerializable("activity_type");

		super.onCreate(savedInstanceState);

		if (activityType == AssignmentInformation) {
			activityTitle = "Assignment";
		} else {
			activityTitle = "Announcement";
			
		}

		if (savedInstanceState == null) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			fragment.setArguments(bundle);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.content_frame, fragment).commit();
		}
	}

	@Override
	public void restoreActionBar() {
		super.restoreActionBar();
		getSupportActionBar().setTitle(activityTitle);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		private DAOFactory df = DAOFactory.getInstance();
		private Assignment assignment;
		private Announcement announcement;

		private AsyncTask<String, Void, String> asyncTask;
		
		TextView textViews[] = null;

		public PlaceholderFragment() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View rootView = null;

			Bundle bundle = getArguments();
			activityType = (ActivityType) bundle
					.getSerializable("activity_type");
			courseID = bundle.getInt("course_id");
			if (activityType == AssignmentInformation) {
				assignmentID = bundle.getInt("assignment_id");
			} else {
				announcementID = bundle.getInt("announcement_id");
			}

			SharedPreferences sp = this.getActivity().getSharedPreferences(
					"CanvasAndroid", Context.MODE_PRIVATE);

			switch (activityType) {
			case Assignment:
				rootView = inflater.inflate(R.layout.fragment_anassignment,
						container, false);

				textViews = new TextView[5];

				textViews[0] = (TextView) rootView
						.findViewById(R.id.anassignment_name);
				textViews[1] = (TextView) rootView
						.findViewById(R.id.anassignment_course);
				textViews[2] = (TextView) rootView
						.findViewById(R.id.anassignment_due_date);
				textViews[3] = (TextView) rootView
						.findViewById(R.id.anassignment_possible_grade);
				textViews[4] = (TextView) rootView
						.findViewById(R.id.anassignment_description);

				AssignmentsDAO assignmentDAO = df.getAssignmentsDAO();
				assignmentDAO.setSharedPreferences(sp);

				assignmentDAO.addInformationListener(new InformationListener() {

					@Override
					public void onComplete(InformationEvent e) {
						AssignmentsDAO ad = (AssignmentsDAO) e.getSource();

						if (!ad.getData().isEmpty()) {
							setAssignment(ad.getData().get(0));
							setProgressGone();
						}
					}
				});

				asyncTask = ((AsyncTask<String, Void, String>) assignmentDAO);
				asyncTask.execute(new String[] { PropertyProvider
								.getProperty("url")
								+ "/api/v1/courses/"
								+ courseID + "/assignments/" + assignmentID });

				break;

			case Announcement:
				rootView = inflater.inflate(R.layout.fragment_anannouncement,
						container, false);

				textViews = new TextView[5];

				textViews[0] = (TextView) rootView
						.findViewById(R.id.anannouncement_title);
				textViews[1] = (TextView) rootView
						.findViewById(R.id.anannouncement_course);
				textViews[2] = (TextView) rootView
						.findViewById(R.id.anannouncement_date);
				textViews[3] = (TextView) rootView
						.findViewById(R.id.anannouncement_author_name);
				textViews[4] = (TextView) rootView
						.findViewById(R.id.anannouncement_message);

				AnnouncementDAO announcementDAO = df.getAnnouncementDAO();
				announcementDAO.setSharedPreferences(sp);

				announcementDAO
						.addInformationListener(new InformationListener() {

							@Override
							public void onComplete(InformationEvent e) {
								AnnouncementDAO ad = (AnnouncementDAO) e
										.getSource();

								if (!ad.getData().isEmpty()) {
									setAnnouncement(ad.getData().get(0));
									InformationActivity im = new InformationActivity();
									setProgressGone();
								}
								
							}
						});

				asyncTask = ((AsyncTask<String, Void, String>) announcementDAO);
				asyncTask.execute(new String[] { PropertyProvider
								.getProperty("url")
								+ "/api/v1/courses/"
								+ courseID
								+ "/discussion_topics/"
								+ announcementID });

				break;
			}

			setProgressVisible(rootView);

			return rootView;
		}

		public Assignment getAssignment() {
			return assignment;
		}

		public void setAssignment(Assignment assignment) {
			this.assignment = assignment;

			if (textViews != null) {
				textViews[0].setText(assignment.getName());
				textViews[1].setText(CourseProvider.getInstance().getCourseWithID(assignment.getCourseId()).getName());
				textViews[2].setText(formatDate(assignment.getDueAt()));
				textViews[3].setText("Maximum grade: "
						+ assignment.getPointsPossible());

				if (assignment.getIsGraded()) {
					textViews[3].append(" (Your grade: "
							+ assignment.getScore() + ")");
				}

				if (assignment.getLockExplanation() != null) {
					textViews[4].setText(assignment.getLockExplanation());
				} else {
					if (assignment.getDescription() != null) {
						textViews[4].setText(Html.fromHtml(assignment
								.getDescription()));
					} else {
						textViews[4].setText("No description");
					}
				}
			}
		}

		public Announcement getAnnouncement() {
			return announcement;
		}

		public void setAnnouncement(Announcement announcement) {
			this.announcement = announcement;

			if (textViews != null) {
				Log.d("Rest","222");
				if (!announcement.getRead_state()){
					Log.d("Rest","Olvasatlan"+ PropertyProvider
							.getProperty("url")
							+ "/api/v1/courses/"
							+ courseID
							+ "/discussion_topics/"
							+ announcementID 
							+ "/read");
					MyService.announcementUnreadCount--; 
					
					new RestInformationDAO().execute(new String[] { PropertyProvider
									.getProperty("url")
									+ "/courses/"
									+ courseID
									+ "/announcements/"
									+ announcementID});
				}
				textViews[0].setText(announcement.getTitle());
				textViews[1].setText(CourseProvider.getInstance().getCourseWithID(announcement.getCourseId()).getName());
				textViews[2].setText(formatDate(announcement.getPostedAt()));
				textViews[3].setText(announcement.getAuthorName());
				textViews[4].setText(Html.fromHtml(announcement.getMessage()));
			}
		}

		private String formatDate(String date) {
			if (!date.startsWith("No")) {
				String newDate = date.substring(0, date.indexOf('Z'));
				newDate = newDate.replace("T", "\n");

				return newDate;
			}

			return date;
		}
		
		@Override
		public void onStop() {
			if ( asyncTask != null && asyncTask.getStatus() == Status.RUNNING) {
				asyncTask.cancel(true);
			}
			super.onStop();
		}

	}

	public static void setProgressVisible(View rootView) {
		progressContainer = rootView.findViewById(R.id.linProg);
		progressContainer.setVisibility(View.VISIBLE);
	}

	public static void setProgressGone() {
		progressContainer.setVisibility(View.GONE);
	}

}
