/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package ldbc.snb.datagen.generator;

import ldbc.snb.datagen.dictionary.Dictionaries;
import ldbc.snb.datagen.objects.*;
import ldbc.snb.datagen.serializer.PersonActivityExporter;
import ldbc.snb.datagen.util.RandomGeneratorFarm;
import ldbc.snb.datagen.vocabulary.SN;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 *
 * @author aprat
 */
public class PhotoGenerator {
	private long postId = 0;
	private LikeGenerator likeGenerator_;
	private Photo photo_;
        private boolean richRdf = false;

	private static final String SEPARATOR = "  ";
	
    public PhotoGenerator(LikeGenerator likeGenerator, Configuration conf) {
		this.likeGenerator_ = likeGenerator;
		this.photo_ = new Photo();
		this.richRdf = conf.getBoolean("ldbc.snb.datagen.generator.richRdf",false);
	}
	public long createPhotos(RandomGeneratorFarm randomFarm, final Forum album, final ArrayList<ForumMembership> memberships, long numPhotos, long startId, PersonActivityExporter exporter) throws IOException {
		long nextId = startId;
		ArrayList<Photo> photos = new ArrayList<Photo>();
		int numPopularPlaces = randomFarm.get(RandomGeneratorFarm.Aspect.NUM_POPULAR).nextInt(DatagenParams.maxNumPopularPlaces + 1);
		ArrayList<Short> popularPlaces = new ArrayList<Short>();
		for (int i = 0; i < numPopularPlaces; i++){
			short aux = Dictionaries.popularPlaces.getPopularPlace(randomFarm.get(RandomGeneratorFarm.Aspect.POPULAR),album.place());
			if(aux != -1) {
				popularPlaces.add(aux);
			}
		}
		for( int i = 0; i< numPhotos; ++i ) {
			int locationId = album.place();
			double latt = 0;
			double longt = 0;
			String locationName = "";
			if (popularPlaces.size() == 0){
				locationName = Dictionaries.places.getPlaceName(locationId);
				latt = Dictionaries.places.getLatt(locationId);
				longt = Dictionaries.places.getLongt(locationId);
			} else{
				int popularPlaceId;
				PopularPlace popularPlace;
				if (randomFarm.get(RandomGeneratorFarm.Aspect.POPULAR).nextDouble() < DatagenParams.probPopularPlaces){
					//Generate photo information from user's popular place
					int popularIndex = randomFarm.get(RandomGeneratorFarm.Aspect.POPULAR).nextInt(popularPlaces.size());
					popularPlaceId = popularPlaces.get(popularIndex);
					popularPlace = Dictionaries.popularPlaces.getPopularPlace(album.place(), popularPlaceId);
					locationName = popularPlace.getName();
					latt = popularPlace.getLatt();
					longt = popularPlace.getLongt();
				} else{
					// Randomly select one places from Album location idx
					popularPlaceId = Dictionaries.popularPlaces.getPopularPlace(randomFarm.get(RandomGeneratorFarm.Aspect.POPULAR),locationId);
					if (popularPlaceId != -1){
						popularPlace = Dictionaries.popularPlaces.getPopularPlace(locationId, popularPlaceId);
						locationName = popularPlace.getName();
						latt = popularPlace.getLatt();
						longt = popularPlace.getLongt();
					} else{
						locationName = Dictionaries.places.getPlaceName(locationId);
						latt = Dictionaries.places.getLatt(locationId);
						longt = Dictionaries.places.getLongt(locationId);
					}
				}
			}
			TreeSet<Integer> tags = new TreeSet<Integer>();
			long date = album.creationDate()+DatagenParams.deltaTime+1000*(i+1);
			/*if( date <= Dictionaries.dates.getEndDateTime() )*/ {
				long id = SN.formId(SN.composeId(nextId++,date));
				photo_.initialize(id,date,album.moderator(), album.id(), "photo"+id+".jpg",tags,album.moderator().ipAddress(),album.moderator().browserId(),latt,longt);
				if (richRdf) {
				    photo_.richRdf(true);
				    if (randomFarm.get(RandomGeneratorFarm.Aspect.PHOTO_MENTIONED).nextDouble() > 0.6) {
					TreeSet<Long> t = new TreeSet<Long>();
                                        // The user mentions one or more (up to 4) members of the forum                                                                                                
                                        t.add(memberships.get(randomFarm.get(RandomGeneratorFarm.Aspect.MEMBERSHIP_INDEX_PHOTO_MENTIONED).nextInt(memberships.size())).person().accountId());                                                
                                        double probabilityForNumberOfMentions = randomFarm.get(RandomGeneratorFarm.Aspect.PHOTO_MENTIONED_NUM).nextDouble();
                                        if (probabilityForNumberOfMentions > 0.5)
                                            t.add(memberships.get(randomFarm.get(RandomGeneratorFarm.Aspect.MEMBERSHIP_INDEX_PHOTO_MENTIONED).nextInt(memberships.size())).person().accountId());
                                        if (probabilityForNumberOfMentions > 0.75)
                                            t.add(memberships.get(randomFarm.get(RandomGeneratorFarm.Aspect.MEMBERSHIP_INDEX_PHOTO_MENTIONED).nextInt(memberships.size())).person().accountId());
                                        if (probabilityForNumberOfMentions > 0.95)
                                            t.add(memberships.get(randomFarm.get(RandomGeneratorFarm.Aspect.MEMBERSHIP_INDEX_PHOTO_MENTIONED).nextInt(memberships.size())).person().accountId());                                  
                                        photo_.mentioned(t);
				    }
				    if (randomFarm.get(RandomGeneratorFarm.Aspect.PHOTO_VISIBILITY).nextDouble() > 0.95) {
                                        if (photo_.mentioned() == null || randomFarm.get(RandomGeneratorFarm.Aspect.PHOTO_VISIBILITY_TF).nextDouble() > 0.5)
                                            photo_.setPublic(true);
                                        else photo_.setPublic(false);
				    }
				}
				if (richRdf && randomFarm.get(RandomGeneratorFarm.Aspect.PHOTO_COUNTRY).nextDouble() > 0.06)
				    photo_.countryKnown(false);
				exporter.export(photo_);
				if( randomFarm.get(RandomGeneratorFarm.Aspect.NUM_LIKE).nextDouble() <= 0.1 ) {
					likeGenerator_.generateLikes(randomFarm.get(RandomGeneratorFarm.Aspect.NUM_LIKE), album, photo_, Like.LikeType.PHOTO, exporter);
				}
			}
		}
		return nextId;
	}
	
}
