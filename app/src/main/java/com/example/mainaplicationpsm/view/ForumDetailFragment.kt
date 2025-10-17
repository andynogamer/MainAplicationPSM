package com.example.mainaplicationpsm

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.adapter.PostAdapter
import com.example.mainaplicationpsm.model.PostProvider
import com.google.android.material.appbar.CollapsingToolbarLayout

class ForumDetailFragment : Fragment() {

    private var forumName: String? = null
    private var forumDesc: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            forumName = it.getString(ARG_FORUM_NAME)
            forumDesc = it.getString(ARG_FORUM_DESC)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forum_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Encontrar el CollapsingToolbarLayout
        val collapsingToolbarLayout: CollapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar)
        collapsingToolbarLayout.title = forumName // Establece el título del foro aquí

        val tvDesc: TextView = view.findViewById(R.id.tvForumDescription)
        val ivMemberIcon: ImageView = view.findViewById(R.id.ivMemberIcon)
        val tvMemberCount: TextView = view.findViewById(R.id.tvMemberCount)
        val recyclerPosts: RecyclerView = view.findViewById(R.id.recyclerForumPosts)

        tvDesc.text = forumDesc
        tvMemberCount.text = "1,234 Miembros"

        // Ajustar la posición de la descripción y miembros para que no se superpongan
        // Estos valores pueden necesitar pequeños ajustes dependiendo de la imagen del banner
        tvDesc.translationY = -70f // Mueve la descripción hacia arriba
        ivMemberIcon.translationY = -70f // Mueve el icono de miembros hacia arriba
        tvMemberCount.translationY = -70f // Mueve el contador de miembros hacia arriba


        recyclerPosts.layoutManager = LinearLayoutManager(requireContext())
        recyclerPosts.adapter = PostAdapter(PostProvider.postList)

        // Opcional: Cargar la imagen del banner con Glide si tienes una URL para el foro
        // val ivForumBanner: ImageView = view.findViewById(R.id.ivForumBanner)
        // Glide.with(requireContext()).load("URL_DEL_BANNER_DEL_FORO").into(ivForumBanner)
    }

    companion object {
        private const val ARG_FORUM_NAME = "forum_name"
        private const val ARG_FORUM_DESC = "forum_desc"

        @JvmStatic
        fun newInstance(name: String, description: String) =
            ForumDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FORUM_NAME, name)
                    putString(ARG_FORUM_DESC, description)
                }
            }
    }
}