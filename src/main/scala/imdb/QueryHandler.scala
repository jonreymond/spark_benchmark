package imdb
import org.apache.spark.rdd.RDD

class QueryHandler(rdd_list : List[RDD[Record]]) {
  // "aka_name"
  val an= rdd_list(0).asInstanceOf[RDD[Aka_name]].cache()
  // "aka_title"
  val aka_title = rdd_list(1).asInstanceOf[RDD[Aka_title]].cache()
  // "cast_info
  val ci = rdd_list(2).asInstanceOf[RDD[Cast_info]].cache()
  // "char_name"
  val chn= rdd_list(3).asInstanceOf[RDD[Char_name]].cache()
  // "comp_cast_type"
  val comp_cast_type = rdd_list(4).asInstanceOf[RDD[Comp_cast_type]].cache()
  // "company_name"
  val cn = rdd_list(5).asInstanceOf[RDD[Company_name]].cache()
  // "company_type"
  val ct= rdd_list(6).asInstanceOf[RDD[Company_type]].cache()
  // "complete_cast"
  val complete_cast = rdd_list(7).asInstanceOf[RDD[Complete_cast]].cache()
  // "info_type"
  val it = rdd_list(8).asInstanceOf[RDD[Info_type]].cache()
  // "keyword"
  val k = rdd_list(9).asInstanceOf[RDD[Keyword]].cache()
  // "kind_type"
  val kind_type = rdd_list(10).asInstanceOf[RDD[Kind_type]].cache()
  // "link_type"
  val lt = rdd_list(11).asInstanceOf[RDD[Link_type]].cache()
  // "movie_companies"
  val mc = rdd_list(12).asInstanceOf[RDD[Movie_companies]].cache()
  // "movie_info_idx"
  val mi_idx = rdd_list(13).asInstanceOf[RDD[Movie_info_idx]].cache()
  // "movie_keyword"
  val mk = rdd_list(14).asInstanceOf[RDD[Movie_keyword]].cache()
  // "movie_link"
  val ml = rdd_list(15).asInstanceOf[RDD[Movie_link]].cache()
  // "name"
  val n= rdd_list(16).asInstanceOf[RDD[Name]].cache()
  // "role_type"
  val rt = rdd_list(17).asInstanceOf[RDD[Role_type]].cache()
  // "title"
  val t = rdd_list(18).asInstanceOf[RDD[Title]].cache()
  // "movie_info"
  val mi = rdd_list(19).asInstanceOf[RDD[Movie_info]].cache()
  // "person_info"
  val pi = rdd_list(20).asInstanceOf[RDD[Person_info]].cache()



    def q1(): (List[Any]) = {

      val ct_f = ct.filter(_.kind=="production companies").map(_.id -> false)
      val it_f = it.filter(_.info=="top 250 rank").map(_.id-> false)
      val mc_f = mc.filter(m => !m.note.contains("as Metro-Goldwyn-Mayer Pictures")
                                && (m.note.contains("co-production") || m.note.contains("presents")))
                    .map(m => m.company_type_id -> (m.movie_id, m.note))
      val mi_idx_s = mi_idx.map(mi => mi.movie_id -> mi.info_type_id)
      val t_s = t.map(tt => tt.id -> (tt.title, tt.production_year))

      // m.movie_id, m.note
      val join1 = ct_f.join(mc_f).map(j1 => j1._2._2)
      //order (t.id=m.movie_id, (t.title, t.production_year), m.note
      val join2 = t_s.join(join1)
      //order t.id,(((t.title, t.production_year), m.note),mi.info_type_id) before map
      val join3 = join2.join(mi_idx_s).map(x => x._2._2 -> (x._2._1))

      val join4 = join3.join(it_f).map(x => x._2._1)

      val res = join4.reduce((a, b) =>
        ((min_s(a._1._1,b._1._1 ), List(a._1._2, b._1._2).min), min_s(a._2, b._2)))


      List(res._1._1, res._1._2, res._2)
    }


  def q2(): List[Any] = {
        val cn_f = cn.filter(_.country_code =="[de]").map(_.id -> false)
        val k_f = k.filter(_.word == "character-name-in-title").map(_.id -> false)
        val mc_f = mc.map(x => x.company_id -> x.movie_id)
        val mk_f = mk.map(x => x.movie_id -> x.word_id)
        val t_f = t.map(x => x.id -> x.title)

        // mc.movie_id -> 0
        val join1 = cn_f.join(mc_f).map(x => x._2._2 -> 0)
        // t.id = mc.movie_id -> t.title
        val join2 = join1.join(t_f).map(x => x._1 -> x._2._2)
        // t.id = mc.movie_id = mk.movie_id -> (t.title, mk.word_id)
        val join3 = join2.join(mk_f).map(x => x._2._2 -> x._2._1)
        val table_res = join3.join(k_f).map(x => x._2._1)
//
        val res = table_res.reduce(min_s)

        List(res)
  }


  def q3(): List[Any] = {
    val k_f = k.filter(_.word.contains("sequel")).map(_.id -> false)
    val mi_f = mi.filter(x => List("Sweden", "Norway", "Germany", "Denmark",
      "Swedish", "Denish", "Norwegian", "German").contains(x.info)).map(_.movie_id -> false)
    val t_f = t.filter(_.production_year > 2005).map(x => x.id -> x.title)
    val mk_f = mk.map(x => x.movie_id -> x.word_id)

    //t_id = mi_movie_id -> t.title
    val join1 = t_f.join(mi_f).map(x => x._1 -> x._2._1)
    //mk.word -> t.title
    val join2 = join1.join(mk_f).map(x => x._2._2 -> x._2._1)
    val table_res = join2.join(k_f).map(_._2._1)
    val res = table_res.reduce(min_s)

    List(res)
  }

  def q4(): List[Any] = {
        val it_f = it.filter(_.info == "rating").map(_.id -> false)
        val k_f = k.filter(_.word.contains("sequel")).map(_.id -> false)
        val mi_idx_f = mi_idx.filter(_.info > "5.0").map(x => x.info_type_id -> (x.movie_id, x.info))
        val mk_f = mk.map(x => x.word_id -> x.movie_id)
        val t_f = t.map(x => x.id -> x.title)
        // mk_movie_id -> false
        val mk_k_j = k_f.join(mk_f).map(x => x._2._2 -> false)
        // mi_idx.movie_id -> mi_idx.info
        val it_idx_j = it_f.join(mi_idx_f).map(x => x._2._2._1 -> x._2._2._2)

        val join1 = mk_k_j.join(it_idx_j).map(x => x._1 -> x._2._2)

        val res_table = t_f.join(join1).map(_._2)

        val res = res_table.reduce((a, b) =>(min_s(a._1,b._1), min_s(a._2, b._2)))

        List(res._1, res._2)
  }

  def q5(): List[Any] = {
        val mi_f = mi.filter(x => List("Sweden", "Norway", "Germany", "Denmark",
                                  "Swedish", "Denish", "Norwegian", "German").contains(x.info))
                                    .map(x => x.info_type_id -> x.movie_id)

        val t_f = t.filter(_.production_year > 2005).map(x => x.id -> x.title)

        val mc_f = mc.filter(x => x.note.contains("(USA)"))
                      .map(x => x.company_type_id -> x.movie_id)
        val ct_f = ct.filter(_.kind == "production companies").map(_.id -> false)
        val it_f = it.map(_.id -> false)

        //mi_movie_id -> false
        val mi_it_j = mi_f.join(it_f).map(_._2._1 -> false)
        //mc.movie_id -> false
        val mc_ct_j = mc_f.join(ct_f).map(_._2._1 -> false)
        //mi_movie_id  = mc.movie_id -> false
        val join1 = mi_it_j.join(mc_ct_j).map(_._1 -> false)

        val table_res = t_f.join(join1).map(_._2._1)
        val res = table_res.reduce(min_s)

//      val res = List("mi_f :" + mi_f.count(),
//        "t_f :" + t_f.count(),
//        "mc_f :" + mc_f.count(),
//        "ct_f :" + ct_f.count(),
//        "it_f :" + it_f.count(),
//        "mi_it_j :" + mi_it_j.count(),
//        "mc_ct_j :" + mc_ct_j.count(),
//        "join1 :" + join1.count(),
//        "table_res :" + table_res.count()
//      )
//    val res = (mc_f.map(_._1).distinct().collect().toList, ct_f.collect().toList)

        List(res)
  }

  def q6(): List[Any] = {
    //simplified since already know keyword
    val k_f = k.filter(_.word.equals("marvel-cinematic-universe")).map(_.id -> false)
    val n_f =n.filter(x => x.name.contains("Downey") || x.name.contains("Robert")).map(x =>x.id -> x.name)
    val t_f = t.filter(_.production_year > 2010).map(x => x.id -> x.title)
    val mk_f = mk.map(x => x.word_id -> x.movie_id)
    val ci_f = ci.map(x => x.person_id ->  x.movie_id)

    //ci_movie_id -> n.name
    val n_ci = n_f.join(ci_f).map(x => x._2._2 -> x._2._1)
    //mk_movie_id -> false
    val k_mk = k_f.join(mk_f).map(_._2._2 -> false)
    //t_id=mk_movie_id -> t_title
    val t_mk = t_f.join(k_mk).map(x => x._1 -> x._2._1)
    val res_table = n_ci.join(t_mk).map(x => x._2)

    val res = res_table.reduce((a, b) =>(min_s(a._1,b._1), min_s(a._2, b._2)))

    List(res._1, res._2)
  }

  def q7(): List[Any] = {
    val an_f = an.filter(_.name.length > 10).map(_.person_id -> false)
    val it_f = it.filter(x => x.info == "mini biography" || x.info == "biographical movies").map(_.id -> false)
    val n_f = n.filter(x => x.name_pcode_cf.length > 0 && x.gender == "ms").map(x => x.id -> x.name)
    val pi_f = pi.filter(_.note.length > 200).map(x => x.info_type_id -> x.person_id)
    val t_f = t.filter(x => x.production_year >= 1980 && x.production_year < 1984).map(x => x.id -> x.title)
    val ci_f = ci.map(x => x.movie_id -> x.person_id)
    val lt_f = lt.filter(x =>List("references", "referenced in", "features", "featured in").contains(x.link)).map(x => x.id -> false)
    val ml_f = ml.map(x =>   x.link_type_id ->x.linked_movie_id)

    //ml_linked_movie ->false
    val lt_ml = lt_f.join(ml_f).map(x => x._2._2 -> false)
    // pi_person_id -> false
    val it_pi = it_f.join(pi_f).map(_._2._2 -> false)
    // t.id = ml.linked -> t.title
    val t_ml = t_f.join(lt_ml).map(x => x._1 -> x._2._1)
    // ci.person_id -> t.title
    val t_ml_ci = t_ml.join(ci_f).map(x => x._2._2 -> x._2._1)
    // ci.person_id = pi.person_id -> t.title
    val ci_pi = it_pi.join(t_ml_ci).map(x => x._1 -> x._2._2)
    // an.person_id = ci.person_id = pi.person_id -> t.title
    val an_ci_pi = ci_pi.join(an_f).map(x => x._1 -> x._2._1)

    val res_table = an_ci_pi.join(n_f).map(x => x._2)

    val res = res_table.reduce((a, b) =>(min_s(a._1,b._1), min_s(a._2, b._2)))
    List(res._1, res._2)
  }

  def q8(): List[Any] = {
    val an_f = an.map(x => x.person_id -> x.name)
    val ci_f = ci.filter(_.note == "(voice: English version)").map(x => x.role_id -> (x.movie_id, x.person_id))
    val cn_f = cn.filter(_.country_code == "[jp]").map(_.id -> false)
    val mc_f = mc.filter(x => x.note.contains("(Japan)") && !x.note.contains("(USA)")).map(x => x.company_id -> x.movie_id)
    val n_f = n.filter(x => x.name.contains("Yo") && !x.name.contains("Yu")).map(_.id -> false)
    val rt_f = rt.filter(_.role == "actress").map(_.id -> false)
    val t_f = t.map(x => x.id -> x.title)

    //  ci.movie_id -> ci.person_id
    val ci_rt = ci_f.join(rt_f).map(x => x._2._1._1 -> x._2._1._2)
    //mc.movie_id -> false
    val mc_cn = mc_f.join(cn_f).map(_._2._1 -> false)
    //t.id = mc.movie_id -> t.title
    val t_mc = mc_cn.join(t_f).map(x => x._1 -> x._2._2)
    // ci.person_id = t.id = mc.movie_id -> t.title
    val ci_t_mc = ci_rt.join(t_mc).map(x => x._2._1 -> x._2._2)
    // n.id = ci.person_id -> t.title
    val ci_n = ci_t_mc.join(n_f).map(x => x._1 -> x._2._1)

    val res_table = ci_n.join(an_f).map(x => (x._2._2, x._2._1))
    val res = res_table.reduce((a, b) =>(min_s(a._1,b._1), min_s(a._2, b._2)))
    List(res._1, res._2)
  }

  def q9(): List[Any] = {
    val ct_f = ct.map(x => x.id -> x.kind)
    val it_f = it.filter(_.info == "bottom 10 rank").map(x => x.id -> false)
    val t_f = t.filter(x => x.production_year >= 2005 && x.production_year < 2010).map(x => x.id -> x.title)
    val mc_f = mc.map(x => x.company_type_id -> (x.movie_id -> x.id))
    val mi_idx_f = mi_idx.map(x => x.info_type_id -> x.movie_id)

    //mi_idx_movie -> false
    val it_mi_idx = it_f.join(mi_idx_f).map(x => x._2._2-> false)
    //mc.movie_id -> mc.id
    val ct_mc = ct_f.join(mc_f).map(x => x._2._2._1 -> (x._2._1, x._2._2._2))
    //mc.movie_id == mi_idx.movie_id -> (ct.kind , mc_id)
    val mc_mi_idx = it_mi_idx.join(ct_mc).map(x => x._1 -> x._2._2)
    //ct.kind -> (mc.id, t.title)
    val join_res = mc_mi_idx.join(t_f).map(x => x._2._1._1 -> (x._2._1._2 , x._2._2))

    val res = join_res.reduceByKey((x, y) => (Math.min(x._1, y._1), min_s(x._2, y._2))).map(_._2).collect().toList
    res
  }

  def q10(): List[Any] = {
    val ct_f = ct.filter(_.kind == "production companies" ).map(_.id -> false)
    val it_f = it.filter(_.info=="top 250 rank").map(_.id -> false)
    val mi_idx_f = mi_idx.map(x => x.info_type_id -> (x.movie_id -> x.info))
    val t_f = t.map(_.id -> false)
    val mc_f = mc.filter(_.note.length > 10).map(x => x.company_type_id -> x.movie_id)

    //(mi.movie_id -> mi.info)
    val mi_it = mi_idx_f.join(it_f).map(x => x._2._1)
    //mc_movieId -> false
    val ct_mc = ct_f.join(mc_f).map(x => x._2._2 -> false)
    //mi.movie_id = mc.movie.id -> mi.info
    val mi_mc = mi_it.join(ct_mc).map(x => x._1 -> x._2._1)
    //mi.info -> 1
    val join_res = mi_mc.join(t_f).map(x => x._2._1 -> 1)

    val res = join_res.reduceByKey(_ + _).sortByKey(ascending = false).collect()

    res.toList
  }

  def q11(): List[Any] = {
    val ci_f = ci.filter(x => List("(voice)", "(voice: Japanese version)",
      "(voice) (uncredited)", "(voice: English version)").contains(x.note))
      .map(x => x.role_id -> (x.person_id -> (x.movie_id -> x.person_role_id)))
    val n_f = n.filter(x => x.gender == "f" && x.name.length > 10).map(_.id -> false)
    val rt_f = rt.filter(_.role == "actress").map(_.id -> false)
    val chn_f = chn.map(x => x.id -> x.name)
    val t_f = t.filter(x => x.production_year >= 1990 && x.production_year < 2010).map(x => x.id -> x.production_year)

    //ci.person_id -> (ci.movie_id -> ci.person_role_id)
    val ci_rt = ci_f.join(rt_f).map(_._2._1)
    //(ci.movie_id -> ci.person_role_id)
    val ci_n = ci_rt.join(n_f).map(_._2._1)
    // ci.person_role_id -> t.production_year
    val ci_t = ci_n.join(t_f).map(_._2)
    //t.production_year -> chn.name
    val res_join = ci_t.join(chn_f).map(_._2)

    val res = res_join.reduceByKey(min_s).sortByKey().collect().toList
    res
  }

  def init_table(s: String): Unit = {
    s match {
      case "q1" => ct.count(); it.count(); mc.count(); mi_idx.count(); mc.count()
      case "q2" => cn.count(); k.count(); mc.count(); mk.count(); t.count();
      case "q3" => k.count(); mi.count(); t.count(); mk.count();
      case "q4" => it.count(); k.count(); mi_idx.count(); mk.count(); t.count();
      case "q5" => mi.count(); t.count(); mc.count(); ct.count(); it.count();
      case "q6" => k.count(); n.count(); t.count(); mk.count(); ci.count();
      case "q7" => an.count(); ci.count(); it.count(); lt.count(); ml.count(); n.count(); pi.count(); t.count();
      case "q8" => an.count(); ci.count(); cn.count(); mc.count(); n.count(); rt.count(); t.count();
      case "q9" => ct.count(); it.count(); t.count(); mc.count(); mi_idx.count();
      case "q10" => ct.count(); it.count(); mc.count(); mi.count(); t.count();
      case "q11" => chn.count(); ci.count(); n.count(); rt.count(); t.count();
      case _ => ???
    }
  }



  def get(s : String): () => List[Any] = {
    s match {
      case "q1" => q1
      case "q2" => q2
      case "q3" => q3
      case "q4" => q4
      case "q5" => q5
      case "q6" => q6
      case "q7" => q7
      case "q8" => q8
      case "q9" => q9
      case "q10"=> q10
      case "q11"=> q11
      case _ => () => ???
    }
  }

//  def getProba(s : String): () => Double = {
//    s match {
//      case "q1" => q1
//      case "q2" => q2
//      case "q3" => q3
//      case "q4" => q4
//      case "q5" => q5
//      case "q6" => q6
//      case _ => () => ???
//    }
//  }
}
