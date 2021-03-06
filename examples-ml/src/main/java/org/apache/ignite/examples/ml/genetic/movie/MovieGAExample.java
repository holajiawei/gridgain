/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.examples.ml.genetic.movie;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.ml.genetic.Chromosome;
import org.apache.ignite.ml.genetic.GAGrid;
import org.apache.ignite.ml.genetic.Gene;
import org.apache.ignite.ml.genetic.parameter.GAConfiguration;
import org.apache.ignite.ml.genetic.parameter.GAGridConstants;

/**
 * In this example, we utilize {@link GAGrid} framework to calculate an optimal set of movies based on our interests
 * in various genres (ie: Action, Comedy, and Romance).
 * <p>
 * Code in this example launches Ignite grid, prepares simple test data (gene pool) and configures GA grid.</p>
 * <p>
 * After that it launches the process of evolution on GA grid and outputs the progress and results.</p>
 * <p>
 * You can change the test data and parameters of GA grid used in this example and re-run it to explore
 * this functionality further.</p>
 * <p>
 * How to run from command line:</p>
 * <p>
 * {@code mvn exec:java -Dexec.mainClass="org.apache.ignite.examples.ml.genetic.movie.MovieGAExample"
 * -DGENRES=Action,Comedy}</p>
 * <p>
 * Remote nodes should always be started with special configuration file which enables P2P class loading: {@code
 * 'ignite.{sh|bat} examples-ml/config/example-ignite.xml'}.</p>
 * <p>
 * Alternatively you can run ExampleNodeStartup in another JVM which will start node with
 * {@code examples-ml/config/example-ignite.xml} configuration.</p>
 */
public class MovieGAExample {
    /**
     * Executes example.
     *
     * Specify value for {@code -DGENRES} JVM system variable.
     *
     * @param args Command line arguments, none required.
     */
    public static void main(String args[]) {
        System.out.println(">>> Movie GA grid example started.");

        List<String> genres = new ArrayList<>();
        String sGenres = "Action,Comedy,Romance";

        StringBuffer sbErrorMsg = new StringBuffer();
        sbErrorMsg.append("GENRES System property not set. Please provide GENRES information.");
        sbErrorMsg.append(" ");
        sbErrorMsg.append("IE: -DGENRES=Action,Comedy,Romance");
        sbErrorMsg.append("\n");
        sbErrorMsg.append("Using default value: Action,Comedy,Romance");

        if (System.getProperty("GENRES") == null)
            System.out.println(sbErrorMsg);
        else
            sGenres = System.getProperty("GENRES");

        StringTokenizer st = new StringTokenizer(sGenres, ",");

        while (st.hasMoreElements()) {
            String genre = st.nextToken();
            genres.add(genre);
        }

        // Create GAConfiguration.
        GAConfiguration gaCfg = new GAConfiguration();

        // Set Gene Pool.
        List<Gene> genes = getGenePool();

        // Define Chromosome.
        gaCfg.setChromosomeLen(3);
        gaCfg.setPopulationSize(100);
        gaCfg.setGenePool(genes);
        gaCfg.setTruncateRate(.10);
        gaCfg.setCrossOverRate(.50);
        gaCfg.setMutationRate(.50);
        gaCfg.setSelectionMtd(GAGridConstants.SELECTION_METHOD.SELECTION_METHOD_TRUNCATION);

        // Create fitness function.
        MovieFitnessFunction function = new MovieFitnessFunction(genres);

        // Set fitness function.
        gaCfg.setFitnessFunction(function);

        try (Ignite ignite = Ignition.start("examples-ml/config/example-ignite.xml")) {
            MovieTerminateCriteria termCriteria = new MovieTerminateCriteria(ignite, System.out::println);

            gaCfg.setTerminateCriteria(termCriteria);

            GAGrid gaGrid = new GAGrid(gaCfg, ignite);

            Chromosome chromosome = gaGrid.evolve();

            System.out.println(">>> Evolution result: " + chromosome);
            System.out.println(">>> Movie GA grid example completed.");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /** */
    private static List<Gene> getGenePool() {
        List<Gene> list = new ArrayList<>();

        Movie movie1 = new Movie();
        movie1.setName("The Matrix");
        movie1.setImdbRating(7);
        List<String> genre1 = new ArrayList<>();
        genre1.add("SciFi");
        genre1.add("Action");
        movie1.setGenre(genre1);
        movie1.setRating("PG-13");
        movie1.setYear("1999");

        Gene gene1 = new Gene(movie1);

        Movie movie2 = new Movie();
        movie2.setName("The Dark Knight");
        movie2.setImdbRating(9.6);
        List<String> genre2 = new ArrayList<>();
        genre2.add("Action");
        movie2.setGenre(genre2);
        movie2.setRating("PG-13");
        movie2.setYear("2008");

        Gene gene2 = new Gene(movie2);

        Movie movie3 = new Movie();
        movie3.setName("The Avengers");
        movie3.setImdbRating(9.6);
        movie3.setYear("2012");

        List<String> genre3 = new ArrayList<>();
        genre3.add("Action");
        movie3.setGenre(genre3);
        movie3.setRating("PG-13");

        Gene gene3 = new Gene(movie3);

        Movie movie4 = new Movie();
        movie4.setName("The Hangover");
        movie4.setImdbRating(7.6);
        List<String> genre4 = new ArrayList<>();
        genre4.add("Comedy");
        movie4.setGenre(genre4);
        movie4.setRating("R");
        movie4.setYear("2009");

        Gene gene4 = new Gene(movie4);

        Movie movie5 = new Movie();
        movie5.setName("The Hangover 2");
        movie5.setImdbRating(9.6);
        List<String> genre5 = new ArrayList<>();
        genre5.add("Comedy");
        movie5.setGenre(genre5);
        movie5.setRating("R");
        movie5.setYear("2012");

        Gene gene5 = new Gene(movie5);

        Movie movie6 = new Movie();
        movie6.setName("This Means War");
        movie6.setImdbRating(6.4);
        List<String> genre6 = new ArrayList<>();
        genre6.add("Comedy");
        genre6.add("Action");
        genre6.add("Romance");
        movie6.setGenre(genre6);
        movie6.setRating("PG-13");
        movie6.setYear("2012");

        Gene gene6 = new Gene(movie6);

        Movie movie7 = new Movie();
        movie7.setName("Hitch");
        movie7.setImdbRating(10);
        List<String> genre7 = new ArrayList<>();
        genre7.add("Comedy");
        genre7.add("Romance");
        movie7.setGenre(genre7);
        movie7.setRating("PG-13");
        movie7.setYear("2005");

        Gene gene7 = new Gene(movie7);

        Movie movie8 = new Movie();
        movie8.setName("21 Jump Street");
        movie8.setImdbRating(6.7);
        List<String> genre8 = new ArrayList<>();
        genre8.add("Comedy");
        genre8.add("Action");
        movie8.setGenre(genre8);
        movie8.setRating("R");
        movie8.setYear("2012");

        Gene gene8 = new Gene(movie8);

        Movie movie9 = new Movie();
        movie9.setName("Killers");
        movie9.setImdbRating(5.1);
        List<String> genre9 = new ArrayList<>();
        genre9.add("Comedy");
        genre9.add("Action");
        genre9.add("Romance");
        movie9.setGenre(genre9);
        movie9.setRating("PG-13");
        movie9.setYear("2010");

        Gene gene9 = new Gene(movie9);

        Movie movie10 = new Movie();
        movie10.setName("What to Expect When You're Expecting");
        movie10.setImdbRating(5.1);
        List<String> genre10 = new ArrayList<>();
        genre10.add("Comedy");
        genre10.add("Romance");
        movie10.setGenre(genre10);
        movie10.setRating("PG-13");
        movie10.setYear("2012");

        Gene gene10 = new Gene(movie10);

        list.add(gene1);
        list.add(gene2);
        list.add(gene3);
        list.add(gene4);
        list.add(gene5);
        list.add(gene6);
        list.add(gene7);
        list.add(gene8);
        list.add(gene9);
        list.add(gene10);

        return list;
    }
}
